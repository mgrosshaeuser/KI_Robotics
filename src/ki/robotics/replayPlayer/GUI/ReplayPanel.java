package ki.robotics.replayPlayer.GUI;

import ki.robotics.replayPlayer.Main;
import ki.robotics.server.robot.virtualRobots.MCLParticle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ReplayPanel extends JPanel{
    private ArrayList<MCLParticle> particles;
    private int factor = 4;

    public ReplayPanel(ArrayList<MCLParticle> particles){
        this.particles = particles;
    }

    public void updateParticles(ArrayList<MCLParticle> particles){
        this.particles = particles;
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        //Map
        g.drawLine(0*factor, 0*factor,150*factor,0*factor);
        g.drawLine(150*factor,0*factor,150*factor,150*factor);
        g.drawLine(150*factor,150*factor,100*factor,150*factor);
        g.drawLine(100*factor,150*factor, 100*factor,200*factor);
        g.drawLine(100*factor,200*factor,0*factor,200*factor);
        g.drawLine(0*factor,200*factor,0*factor,0*factor);
        g.drawLine(0*factor,100*factor,50*factor,100*factor);
        g.drawLine(50*factor,50*factor,50*factor,0*factor);
        g.drawLine(150*factor,50*factor,100*factor,50*factor);
        g.drawLine(0*factor,150*factor,50*factor,150*factor);
        g.drawLine(100*factor,50*factor,100*factor,100*factor);

        for(MCLParticle particle : particles){
            particle.paint(g, 4, 4,0,0, 0);
        }

    }
}
