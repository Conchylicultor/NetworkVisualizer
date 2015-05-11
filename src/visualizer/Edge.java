package visualizer;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.util.Pair;

public class Edge {
    
    private float weight;
    
    private Pair<Vertex> endpointsSave;

    public Edge() {
        this(0.f);
    }
    
    public Edge(float weight) {
        this.weight = weight;
    }

	public static Factory<Edge> getFactory() {
		return new Factory<Edge>() {
            public Edge create() {
                return new Edge();
            }
        };
	}

    public static Transformer<Edge, Paint> getColorTransformer() {
        // We only plot edges with strong weight
        return new Transformer<Edge, Paint>() {
            public Paint transform(Edge i) {
                return Color.BLACK;
            }
        };
    }
    
    public float getWeight() {
        return weight;
    }

	public void setWeight(float floatValue) {
	    weight = floatValue;
	}

    public Pair<Vertex> getEndpointsSave() {
        return endpointsSave;
    }

    public void setEndpointsSave(Pair<Vertex> endpointsSave) {
        this.endpointsSave = endpointsSave;
    }

}
