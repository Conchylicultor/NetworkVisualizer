package visualizer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
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
    
    private boolean isHidden;
    
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
        graphics.drawImage(bufferIcon, 0, 0, plottedIconWidth, plottedIconHeight, null);

        /*if(plotNameColor || isSelected || nbNeighborSelected > 0)
        {
            Graphics2D g = (Graphics2D) newImage.getGraphics();
            g.setStroke(new BasicStroke(4.f));
            if(isSelected)
            {
                g.setColor(new Color(255, 25, 34));
            }
            else if (nbNeighborSelected > 0)
            {
                g.setColor(new Color(55, 255, 124));
            }
            else if(plotNameColor)
            {
                g.setStroke(new BasicStroke(8.f));
                int valueR = 125;
                int valueG = 125;
                int valueB = 125;
                if(persName != null && !persName.isEmpty())
                {
                    int hascode = persName.hashCode();
                    valueR = hascode%7777;
                    valueR = valueR*valueR; // Avoid negative number
                    valueG = hascode%8888;
                    valueG = valueG*valueG;
                    valueB = hascode%9999;
                    valueB = valueB*valueB;
                }

                Color colorBorder = new Color(valueR % 255,
                                              valueG % 255,
                                              valueB % 255);
                g.setColor(colorBorder);
            }
            g.drawRect(0, 0, newImage.getWidth(), newImage.getHeight());
        }*/
        
        graphics.dispose();
        
        plottedIcon = new LayeredIcon(new ImageIcon(tempIcon).getImage());
    }

}
