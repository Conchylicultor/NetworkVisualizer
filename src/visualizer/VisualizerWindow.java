package visualizer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.util.Animator;

public class VisualizerWindow extends JFrame {

    private Graph<Vertex, Edge> sequenceGraph;
    private VisualizationViewer<Vertex, Edge> networkCanvas;

    
    private final List<Vertex> pickedVertexList = new ArrayList<Vertex>();
    private PickedState<Vertex> pickedState;
    
    private JComboBox<String> persNameField;
    private JButton selectAllSimilarButton;
    private JButton disconnectButton;
    private JButton mergeButton;
    private JCheckBox hideNamedSequence;
    private JCheckBox plotNameColors;
    private JSlider minDateSlider;
    private JSlider maxDateSlider;
    private JSlider weightFilterSlider;
    private JComboBox<String> layoutChoiceComboBox;
    private JButton adjustGravityButton;
    
    private SequenceImagesPane sequenceImagesPane;
    
    private JButton saveButton;
    
	public VisualizerWindow() {
		// ----- General informations -----
		
		super("Sequence network");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ----- The main graph area -----

        // Create the canvas
		loadGraph();
        networkCanvas = new VisualizationViewer<Vertex, Edge>(new KKLayout<Vertex, Edge>(sequenceGraph));
        
        // Modifier (color, image,...)
        networkCanvas.getRenderContext().setVertexIconTransformer(Vertex.getIconTransformer());
        networkCanvas.getRenderContext().setVertexLabelTransformer(Vertex.getLabelTransformer());
        networkCanvas.getRenderContext().setEdgeDrawPaintTransformer(Edge.getColorTransformer());

        // Mouse control
        PluggableGraphMouse pluggableMouse = new PluggableGraphMouse();
        pluggableMouse.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON2_MASK));
        pluggableMouse.add(new ScalingGraphMousePlugin(new ViewScalingControl(), 0, 1.1f, 0.9f));
        pluggableMouse.add(new PickingGraphMousePlugin<Vertex, Edge>());
        networkCanvas.setGraphMouse(pluggableMouse);
		
		// ----- The right control panel -----
        
        loadControls();

        JScrollPane sequenceImagesScrollPane = new JScrollPane(sequenceImagesPane, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
        controlPanel.setPreferredSize(new Dimension(250, Integer.MAX_VALUE));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        BoxLayout controlPanelLayout = new BoxLayout(controlPanel, BoxLayout.Y_AXIS);
        controlPanel.setLayout(controlPanelLayout);

        final int spacerSize = 8;
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Sequence(s) name: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(persNameField);
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Controls: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(selectAllSimilarButton);
        controlPanel.add(disconnectButton);
        controlPanel.add(mergeButton);
        controlPanel.add(hideNamedSequence);
        controlPanel.add(plotNameColors);
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(layoutChoiceComboBox);
        controlPanel.add(adjustGravityButton);
        if(Vertex.getMaxDate() != 0)
        {
            controlPanel.add(Box.createVerticalStrut(spacerSize));
            controlPanel.add(new JLabel("Date Filter: "));
            controlPanel.add(Box.createVerticalStrut(spacerSize));
            controlPanel.add(minDateSlider);
            controlPanel.add(maxDateSlider);
        }
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Weight Filter: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(weightFilterSlider);
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Sequence images: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(sequenceImagesScrollPane);
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(saveButton);
        
        // ----- Assemble the global ui -----
        
        BoxLayout mainLayout = new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS);
        this.getContentPane().setLayout(mainLayout);
        this.getContentPane().add(networkCanvas);
        this.getContentPane().add(controlPanel);
        
        this.pack();
	}
	
	private void loadGraph()
	{
    	// Our graph
	    sequenceGraph = new UndirectedSparseGraph<Vertex, Edge>();
        
        // Will Create and load the vertex/edge created with the factory
        PajekNetReader<Graph<Vertex, Edge>, Vertex, Edge> fileReader = new PajekNetReader<Graph<Vertex, Edge>, Vertex, Edge>(Vertex.getFactory(), Edge.getFactory());
        
        fileReader.setVertexLabeller(new MapSettableTransformer<Vertex, String>(new HashMap<Vertex, String>()));
        fileReader.setEdgeWeightTransformer(new MapSettableTransformer<Edge, Number>(new HashMap<Edge, Number>()));
        
        try {
            fileReader.load("/home/etienne/__A__/Dev/Reidentification/Data/OutputReid/network.net", sequenceGraph);
        } catch (IOException e) {
            System.err.println("Cannot load the network file");
            e.printStackTrace();
        }

		for (Vertex node : sequenceGraph.getVertices())
		{
			node.setLabel(fileReader.getVertexLabeller().transform(node));
		}
		
		for (Edge edge : sequenceGraph.getEdges())
		{
			edge.setWeight(fileReader.getEdgeWeightTransformer().transform(edge).floatValue());
		}
	}

	private void loadControls()
	{
	    pickedState = networkCanvas.getPickedVertexState();
        // Attach the listener that will print when the vertices selection changes.
        pickedState.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object subject = e.getItem();
                // The graph uses Integers for vertices.
                if (subject instanceof Vertex)
                {
                    Vertex vertex = (Vertex) subject;
                    if (pickedState.isPicked(vertex))
                    {
                        // Update the model
                        pickedVertexList.add(vertex);
                        
                        // Update the icons
                        vertex.setSelected(true);
                        for(Vertex neighborVertex : sequenceGraph.getNeighbors(vertex))
                        {
                            neighborVertex.neighborSelected();
                        }
                        
                        // Update the sequence pane
                        sequenceImagesPane.setSequence(vertex.getImageIdList());
                    }
                    else
                    {
                        // Update the model
                        pickedVertexList.remove(vertex);
                        
                        // Update the icons
                        vertex.setSelected(false);
                        for(Vertex neighborVertex : sequenceGraph.getNeighbors(vertex))
                        {
                            neighborVertex.neighborDeselected();
                        }
                        
                        // Update the sequence pane
                        if(pickedVertexList.isEmpty())
                        {
                            sequenceImagesPane.setSequence(null);
                        }
                    }
                    // The parent function will repaint the graph
                }
            }
        });
        
        
        persNameField = new JComboBox<String>();
        persNameField.setEditable(true);
        persNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, persNameField.getPreferredSize().height));
        persNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        persNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Rename the current selection
                String currentText = ((JTextField)persNameField.getEditor().getEditorComponent()).getText();
                for(Vertex currentVertex : pickedVertexList)
                {
                    currentVertex.setPersName(currentText);
                }
                
                // Add the name to the list (if not already present)
                ComboBoxModel<String> model = persNameField.getModel();
                boolean found = false;
                for(int i=0 ; i<model.getSize() ; i++) {
                    if(currentText.equals((String)model.getElementAt(i)))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    persNameField.addItem(currentText);
                }
                
                networkCanvas.repaint();
            }
        });
        
        
        selectAllSimilarButton = new JButton("Select all similar");
        selectAllSimilarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(pickedVertexList.size() > 0)
                {
                    Vertex first = pickedVertexList.get(0); // We pick the first selected person
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        if(currentVertex.getPersName().equals(first.getPersName()))
                        {
                            pickedState.pick(currentVertex, true); // Will repaint the graph
                        }
                    }
                }
            }
        });
        
        
        disconnectButton = new JButton("Disconnect selection");
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

                List<Edge> listRemove = new ArrayList<Edge>();
                // For all selected nodes
                for(Vertex pickedVertex : pickedVertexList)
                {
                    // We check if some neighbor are not selected
                    for(Vertex neighborVertex : sequenceGraph.getNeighbors(pickedVertex))
                    {
                        if(!pickedVertexList.contains(neighborVertex))
                        {
                            neighborVertex.neighborDeselected();
                            listRemove.add(sequenceGraph.findEdge(pickedVertex, neighborVertex));
                        }
                    }
                }
                for(Edge currentEdge : listRemove)
                {
                    sequenceGraph.removeEdge(currentEdge);
                }
                
                networkCanvas.repaint();
            }
        });
        
        
        mergeButton = new JButton("Merge together");
        mergeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final float maxWeightValue = 2.f; // TODO : Define max value (Warning: either update the edge filter after or define this value as not filterable)
                for(Vertex vertexI : pickedVertexList)
                {
                    for(Vertex vertexJ : pickedVertexList)
                    {
                        if(vertexI != vertexJ)
                        {
                            if(sequenceGraph.findEdge(vertexI, vertexJ) != null)
                            {
                                sequenceGraph.findEdge(vertexI, vertexJ).setWeight(maxWeightValue); // Set max weight
                            }
                            else
                            {
                                sequenceGraph.addEdge(new Edge(maxWeightValue), vertexI, vertexJ);
                                vertexI.neighborSelected();
                                vertexJ.neighborSelected();
                            }
                        }
                    }
                }
                
                networkCanvas.repaint();
            }
        });
        
        
        hideNamedSequence = new JCheckBox("Hide all named sequences");
        hideNamedSequence.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(hideNamedSequence.isSelected())
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setIsHidden(true);
                    }
                }
                else
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setIsHidden(false);
                    }
                }
                networkCanvas.repaint();
            }
        });
        
        
        plotNameColors = new JCheckBox("Show colors of named sequences");
        plotNameColors.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(plotNameColors.isSelected())
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setPlotNameColor(true);
                    }
                }
                else
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setPlotNameColor(false);
                    }
                }
                networkCanvas.repaint();
            }
        });
        
        
        layoutChoiceComboBox = new JComboBox<String>();
        layoutChoiceComboBox.addItem("KKLayout");
        layoutChoiceComboBox.addItem("FRLayout");
        layoutChoiceComboBox.addItem("SpringLayout");
        layoutChoiceComboBox.addItem("ISOMLayout");
        layoutChoiceComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, persNameField.getPreferredSize().height));
        layoutChoiceComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        layoutChoiceComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLayoutGraph();
            }
        });
        
        
        adjustGravityButton = new JButton("Adjust gravity");
        adjustGravityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                updateLayoutGraph();
            }
        });
        
        
        minDateSlider = new JSlider(Vertex.getMinDate(), Vertex.getMaxDate(), Vertex.getMinDate());
        minDateSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        minDateSlider.setMajorTickSpacing(60*60);
        minDateSlider.setMinorTickSpacing(60);
        minDateSlider.setPaintTicks(true);

        maxDateSlider = new JSlider(Vertex.getMinDate(), Vertex.getMaxDate(), Vertex.getMaxDate());
        maxDateSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        maxDateSlider.setMajorTickSpacing(60*60);
        maxDateSlider.setMinorTickSpacing(60);
        maxDateSlider.setPaintTicks(true);
        
        minDateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!minDateSlider.getValueIsAdjusting()) {
                    unpickAll();
                    updateFilterVertexGraph();
                    updateFilterEdgeGraph();
                    networkCanvas.repaint();
                }
            }
        });
        
        maxDateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!maxDateSlider.getValueIsAdjusting()) {
                    unpickAll();
                    updateFilterVertexGraph();
                    updateFilterEdgeGraph();
                    networkCanvas.repaint();
                }
            }
        });
        
        
        weightFilterSlider = new JSlider(0, 200, 0);
        weightFilterSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        weightFilterSlider.setMajorTickSpacing(100);
        weightFilterSlider.setMinorTickSpacing(20);
        weightFilterSlider.setPaintTicks(true);
        weightFilterSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!weightFilterSlider.getValueIsAdjusting()) {
                    unpickAll();
                    updateFilterEdgeGraph();
                    networkCanvas.repaint();
                }
            }
        });
        
        
        sequenceImagesPane = new SequenceImagesPane();
        
        
        saveButton = new JButton("Save (current)");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try 
                {
                    FileWriter saveFile = new FileWriter("/home/etienne/__A__/Dev/Reidentification/Data/OutputReid/network_save.net");
                    
                    saveFile.write("*Vertices " + sequenceGraph.getVertexCount() + "\n");
                    int i = 1;
                    for(Vertex vertex : sequenceGraph.getVertices())
                    {
                        vertex.setId(i);
                        saveFile.write(i + " \"" + vertex.getLabel() + "\"\n");
                        i++;
                    }

                    saveFile.write("*Edges\n");
                    for(Edge edge : sequenceGraph.getEdges())
                    {
                        Pair<Vertex> pair = sequenceGraph.getEndpoints(edge);
                        saveFile.write(pair.getFirst().getId() + " " + pair.getSecond().getId() + " " + edge.getWeight() + "\n");
                    }
                    
                    saveFile.close();
                }
                catch (IOException e) 
                {
                    System.err.println("Cannot save the network (path incorrect)");
                    e.printStackTrace();
                }
            }
        });
	}
	
    private void updateLayoutGraph()
    {
        Layout<Vertex,Edge> newlayout = null;
        if(((String)layoutChoiceComboBox.getSelectedItem()).equals("FRLayout"))
        {
            newlayout = new FRLayout<Vertex,Edge>(sequenceGraph);
            ((FRLayout<Vertex,Edge>)newlayout).setMaxIterations(200);
        }
        else if(((String)layoutChoiceComboBox.getSelectedItem()).equals("KKLayout"))
        {
            newlayout = new KKLayout<Vertex,Edge>(sequenceGraph);
            ((KKLayout<Vertex,Edge>)newlayout).setMaxIterations(200);
        }
        else if(((String)layoutChoiceComboBox.getSelectedItem()).equals("SpringLayout"))
        {
            newlayout = new SpringLayout<Vertex,Edge>(sequenceGraph);
        }
        else if(((String)layoutChoiceComboBox.getSelectedItem()).equals("ISOMLayout"))
        {
            newlayout = new ISOMLayout<Vertex,Edge>(sequenceGraph);
        }
        else
        {
            throw new Error("Error choice: wrong layout name");
        }
        newlayout.setInitializer(networkCanvas.getGraphLayout());
        newlayout.setSize(networkCanvas.getSize());
        
        LayoutTransition<Vertex,Edge> transition =
            new LayoutTransition<Vertex,Edge>(networkCanvas, networkCanvas.getGraphLayout(), newlayout);
        Animator animator = new Animator(transition);
        animator.start();
        
        networkCanvas.getRenderContext().getMultiLayerTransformer().setToIdentity(); // What is the use of those lines ?
        networkCanvas.repaint();
    }
	
    Collection<Vertex> filteredVertexList = new LinkedHashSet<>();
    Collection<Edge> filteredEdgeList = new LinkedHashSet<>(); // LinkedHashSet automatically remove the duplicate
    
    private void unpickAll()
    {
        Collection<Vertex> pickedVertex = new HashSet<Vertex>(pickedState.getPicked());
        for(Vertex vertex : pickedVertex)
        {
            pickedState.pick(vertex, false);
        }
    }
    
    private void updateFilterVertexGraph()
    {
        List<Vertex> vertexToRemoveList = new ArrayList<Vertex>();
        
        // ----- Filter new vertices -----
        for(Vertex vertex : sequenceGraph.getVertices())
        {
            // TODO: Filter also if "hide if selected" is selected ?
            if(vertex.getDate() < minDateSlider.getValue() || vertex.getDate() > maxDateSlider.getValue()) // Out of range
            {
                filteredVertexList.add(vertex);
                vertexToRemoveList.add(vertex);

                // Filter the associated edges
                for(Edge edge : sequenceGraph.getIncidentEdges(vertex))
                {
                    filteredEdgeList.add(edge);
                    edge.setEndpointsSave(sequenceGraph.getEndpoints(edge));
                    // Not necessary to remove from the graph, the edges are removed when we remove the vertex
                }
            }
        }
        
        for(Vertex vertex : vertexToRemoveList)
        {
            sequenceGraph.removeVertex(vertex);
        }
        vertexToRemoveList.clear();
        
        // ----- Restore the filtered vertices -----
        for(Vertex vertex : filteredVertexList)
        {
            if(vertex.getDate() >= minDateSlider.getValue() && vertex.getDate() <= maxDateSlider.getValue()) // In range
            {
                sequenceGraph.addVertex(vertex);
                vertexToRemoveList.add(vertex);
            }
        }
        
        filteredVertexList.removeAll(vertexToRemoveList);
    }
    
    private void updateFilterEdgeGraph()
    {
        float thresholdValue = weightFilterSlider.getValue() / 100.f;
        
        List<Edge> edgesToRemoveList = new ArrayList<>();
        
        // ----- Filter new edges -----
        for(Edge edge : sequenceGraph.getEdges())
        {
            if(edge.getWeight() < thresholdValue) // Filter if weight too small
            {
                filteredEdgeList.add(edge);
                edge.setEndpointsSave(sequenceGraph.getEndpoints(edge));// Save the associated vertex
                
                edgesToRemoveList.add(edge);
            }
        }
        
        for(Edge edge : edgesToRemoveList)
        {
            sequenceGraph.removeEdge(edge);
        }
        edgesToRemoveList.clear();
        
        // ----- Restore the filtered edges -----
        for(Edge edge : filteredEdgeList)
        {
            if(edge.getWeight() >= thresholdValue && // In range
               sequenceGraph.containsVertex(edge.getEndpointsSave().getFirst()) &&
               sequenceGraph.containsVertex(edge.getEndpointsSave().getSecond())) // and both endpoints presents
            {
                sequenceGraph.addEdge(edge, edge.getEndpointsSave());

                edge.setEndpointsSave(null);
                edgesToRemoveList.add(edge);
            }
        }
        
        filteredEdgeList.removeAll(edgesToRemoveList);
    }
}
