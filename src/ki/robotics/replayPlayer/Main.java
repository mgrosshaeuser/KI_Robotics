package ki.robotics.replayPlayer;

import ki.robotics.replayPlayer.GUI.ReplayPanel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[]args) throws InterruptedException {
        ArrayList<ParticleSet> particleSets = fileToSets();

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200,1000);
        f.setVisible(true);
        ReplayPanel replayPanel = new ReplayPanel(particleSets.get(0).getParticles());
        f.add(replayPanel);

        for(ParticleSet p:particleSets){
            Thread.sleep(500);
            replayPanel.updateParticles(p.getParticles());
            f.repaint();
        }

    }


    //TODO: Filepath übergeben
    //Wandelt übergebene Datei wieder in Partikel um
    public static ArrayList<ParticleSet> fileToSets() {
        String file = "201805221938.txt";
        ArrayList<ParticleSet> particleSets = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            line = br.readLine();
            while((line = br.readLine()) != null) {
                ArrayList<MCLParticle> particles = new ArrayList<>();
                //Daten in einzelne Partikel auftrennen
                String[] particleStrings = line.split(";");
                for (String s : particleStrings) {

                    //einzelne Partikel in die Komponenten auftrennen
                    String[] componentsTemp = s.split(" ");

                    //Überflüssiges particleX: entfernen
                    String[] components = new String[componentsTemp.length - 1];
                    System.arraycopy(componentsTemp, 1, components, 0, componentsTemp.length - 1);
                    Float[] data = new Float[4];
                    Color color = null;
                    int i = 0;
                    for (String component : components) {
                        //Komponenten Bestandteile zerlegen, wir brauchen nur die Daten
                        if(i==4){ //Color rausfiltern
                            int[] colorInts = new int[3];
                            int j=0;
                            String[] colorComponents = component.split(",");
                            for(String colorComponent : colorComponents){
                                String temp = colorComponent.replaceAll("[^0-9]", "");
                                colorInts[j++] = Integer.parseInt(temp);
                            }
                            color = new Color(colorInts[0],colorInts[1],colorInts[2]);
                        }else{
                            String[] componentParts = component.split(":");
                            data[i++] = Float.parseFloat(componentParts[1]);
                        }


                    }

                    MCLParticle particle = new MCLParticle(data[0],data[1],data[2],data[3],color);
                    particles.add(particle);
                }

                particleSets.add(new ParticleSet(particles));
                br.readLine();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return particleSets;
    }


    public static class ParticleSet{
        private ArrayList<MCLParticle> particles;
        private ParticleSet(ArrayList<MCLParticle> particles){
            this.particles = particles;
        }

        private void setParticles(ArrayList<MCLParticle> particles){
            this.particles=particles;
        }
        private ArrayList<MCLParticle> getParticles(){
            return particles;
        }

    }
}
