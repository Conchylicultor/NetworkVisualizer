package visualizer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SequenceImagesPane extends JPanel
{

    private List<String> currentSeqIdList;
    private Timer countdownTimer;

    private List<BufferedImage> imagesList;
        
    public SequenceImagesPane()
    {
        imagesList = new ArrayList<BufferedImage>();
        
        countdownTimer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                imagesList.clear();
                
                if(currentSeqIdList != null)
                {
                    try {
                        int step = currentSeqIdList.size()/6;
                        if (step == 0)
                        {
                            step = 1;
                        }
                        int compomentWidth = 0;
                        for (int i = 0; i < currentSeqIdList.size(); i += step) {
                            imagesList.add(ImageIO.read(new File("/home/etienne/__A__/Dev/Reidentification/Data/Traces/" + currentSeqIdList.get(i) + ".png")));
                            compomentWidth += imagesList.get(imagesList.size() - 1).getWidth();
                        }
                        setPreferredSize(new Dimension(compomentWidth, getPreferredSize().height));
                        setSize(new Dimension(compomentWidth, getPreferredSize().height));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                repaint();
            }
        });
        countdownTimer.setRepeats(false);
    }
    
    public void setSequence(List<String> newSeqId)
    {
        currentSeqIdList = newSeqId;
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
