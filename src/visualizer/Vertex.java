package visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.LayeredIcon;

public class Vertex {
    private String persName;
    private String seqId;
    
    private BufferedImage bufferIcon;
    private Icon plottedIcon; // Icon after modifications (adding borders,...)
    
    private boolean isHidden = false;
    private boolean plotNameColor = false;
    private boolean isSelected = false;
    private int nbNeighborSelected = 0;
    
    public static Factory<Vertex> getFactory() {
        return new Factory<Vertex>() {
            public Vertex create() {
                return new Vertex();
            }
        };
    }

    public static Transformer<Vertex, Icon> getIconTransformer() {
        return new Transformer<Vertex, Icon>() {
            public Icon transform(final Vertex v) {
                return v.plottedIcon;
            }
        };
    }

    public static Transformer<Vertex, String> getLabelTransformer() {
        return new Transformer<Vertex, String>() {
            @Override
            public String transform(Vertex v) {
                if(v.persName != null && !v.persName.isEmpty())
                {
                    return v.persName;
                }
                return ""; // Undefined
            }
        };
    }

    public void setLabel(String transform) {
        Pattern persPattern = Pattern.compile("pers:(\\S+)");
        Pattern seqPattern = Pattern.compile("seq:(\\S+)");
        Matcher persMatcher = persPattern.matcher(transform);
        Matcher seqMatcher = seqPattern.matcher(transform);

        if(persMatcher.find())
        {
            persName = persMatcher.group(1);
        }
        if(seqMatcher.find())
        {
            seqId = seqMatcher.group(1);
            
            // Load icon
            String name = "/home/etienne/__A__/Dev/Reidentification/Data/Traces/" + seqId + ".png";
            try {
                bufferIcon = ImageIO.read(new File(name)); // Buffer will contain the original image (before transformations)

                updateIcon();
            } catch(Exception ex) {
                System.err.println("Cannot load " + name);
                System.err.println(ex.getMessage());
            }
        }
    }

    private void updateIcon() {
        int plottedIconWidth = 40;
        int plottedIconHeight = 100;
        
        // We reduce the icon size of named persons
        if(isHidden && persName != null && !persName.isEmpty())
        {
            plottedIconWidth /= 10;
            plottedIconHeight /= 10;
        }
        
        BufferedImage tempIcon = new BufferedImage(plottedIconWidth, plottedIconHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D graphics = (Graphics2D)tempIcon.createGraphics();
        
        // Plot the icon
        graphics.drawImage(bufferIcon, 0, 0, plottedIconWidth, plottedIconHeight, null);

        // Plot the border
        if(plotNameColor || isSelected || nbNeighborSelected > 0)
        {
            graphics.setStroke(new BasicStroke(8.f));
            
            if(isSelected)
            {
                graphics.setColor(new Color(255, 0, 0));
            }
            else if (nbNeighborSelected > 0)
            {
                graphics.setColor(new Color(0, 200, 0));
            }
            else if(plotNameColor)
            {
                // Default color value
                int valueR = 125;
                int valueG = 125;
                int valueB = 125;
                
                if(persName != null && !persName.isEmpty())
                {
                    Random r = new Random(persName.hashCode());
                    valueR = r.nextInt(256);
                    valueG = r.nextInt(256);
                    valueB = r.nextInt(256);
                }

                Color colorBorder = new Color(valueR,
                                              valueG,
                                              valueB);
                graphics.setColor(colorBorder);
            }
            
            graphics.drawRect(0, 0, tempIcon.getWidth(), tempIcon.getHeight());
        }
        
        graphics.dispose();
        
        plottedIcon = new LayeredIcon(new ImageIcon(tempIcon).getImage());
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
        if(persName != null && !persName.isEmpty())
            updateIcon();
    }
    
    public void setPlotNameColor(boolean plotNameColor) {
        this.plotNameColor = plotNameColor;
        updateIcon();
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        updateIcon();
    }

    public void neighborSelected() {
        nbNeighborSelected++;
        if(nbNeighborSelected == 1) // Only update the first time
            updateIcon();
    }

    public void neighborDeselected() {
        nbNeighborSelected--;
        if(nbNeighborSelected == 0) // Only update the first time
            updateIcon();
    }

    public String getSeqId() {
        if(seqId == null)
            return "";
        return seqId;
    }

    public String getPersName() {
        if(persName == null)
            return "";
        return persName;
    }

    public void setPersName(String persName) {
        this.persName = persName;
        if(plotNameColor || isHidden)
            updateIcon();
    }

}
