package visualizer;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.Timer;

public class SequenceImagesPane extends JPanel
{

    private String currentSeqId;
    private Timer countdownTimer;
    
    private List<String> imagesList;
    
    public SequenceImagesPane()
    {
        // Get the sequence image list
        imagesList = new ArrayList<String>();

        File[] imagesFiles = new File("/home/etienne/__A__/Dev/Reidentification/Data/Traces/").listFiles();
        if(imagesFiles == null)
        {
            System.err.println("Cannot get the sequence images list");
        }

        for (File file : imagesFiles)
        {
            if (file.isFile() && file.getName().endsWith(".png"))
            {
                imagesList.add(file.getName());
            }
        }
        
        countdownTimer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Pattern seqPattern = Pattern.compile("(\\d+_\\d+)_(\\d+)");
                Matcher seqMatcher = seqPattern.matcher(currentSeqId);

                if(seqMatcher.find())
                {
                    String baseStr = seqMatcher.group(1);
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
    }

}
