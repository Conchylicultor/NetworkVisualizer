package visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SequenceImagesPane extends JPanel
{

    private String currentSeqId;
    private Timer countdownTimer;

    private List<BufferedImage> imagesList;
    
    private String imagesPath = "/home/etienne/__A__/Dev/Reidentification/Data/Traces/";
    private List<String> imagesNameList;
    
    public SequenceImagesPane()
    {
        imagesList = new ArrayList<BufferedImage>();
        
        // Get the sequence image list
        imagesNameList = new ArrayList<String>();

        File[] imagesFiles = new File(imagesPath).listFiles();
        Arrays.sort(imagesFiles); // Sort in alphabetical order
        if(imagesFiles == null)
        {
            System.err.println("Cannot get the sequence images list");
        }

        for (File file : imagesFiles)
        {
            if (file.isFile() && file.getName().endsWith(".png") && !file.getName().endsWith("_mask.png"))
            {
                imagesNameList.add(file.getName());
            }
        }
        
        System.out.println(imagesNameList.size() + " sequence images retrieved");
        
        countdownTimer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                imagesList.clear();
                
                try {
                    // Get the list of all image in the sequence
                    Pattern seqPattern = Pattern.compile("(\\d+_\\d+)_(\\d+)");
                    Matcher seqMatcher = seqPattern.matcher(currentSeqId);
    
                    String baseStr;
                    if(!seqMatcher.find())
                    {
                        throw new Exception("Unknown selected sequence: " + currentSeqId);
                    }
                    baseStr = seqMatcher.group(1);
                    
                    List<String> sequenceImageStrings =new ArrayList<String>();
                    for (String imageName : imagesNameList) {
                        if(imageName.startsWith(baseStr))
                        {
                            sequenceImageStrings.add(imageName);
                        }
                    }
                    
                    int step = sequenceImageStrings.size()/6;
                    if (step == 0)
                    {
                        step = 1;
                    }
                    int compomentWidth = 0;
                    for (int i = 0; i < sequenceImageStrings.size(); i += step) {
                        imagesList.add(ImageIO.read(new File(imagesPath + sequenceImageStrings.get(i))));
                        compomentWidth += imagesList.get(imagesList.size() - 1).getWidth();
                    }
                    setPreferredSize(new Dimension(compomentWidth, getPreferredSize().height));
                    setSize(new Dimension(compomentWidth, getPreferredSize().height));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    // Nothing to do
                }

                repaint();
            }
        });
        countdownTimer.setRepeats(false);
    }
    
    public void setSequence(String newSeqId)
    {
        currentSeqId = newSeqId;
        countdownTimer.restart();
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int x = 0;
        for (BufferedImage bufferedImage : imagesList) {
            g.drawImage(bufferedImage, x, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
            x += bufferedImage.getWidth();
        }
    }

}
