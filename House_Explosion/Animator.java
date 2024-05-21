/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab5;
import java.util.TimerTask;
/**
 *
 * @author srosenbe
 */
public class Animator extends TimerTask
{
    VectorGraphics vgReference;

    public Animator(VectorGraphics vg)
    {
        vgReference = vg;
    }

    public void run()
    {
        for(int i = 0; i < vgReference.flyingThings.size(); i++)
        {
            VectorGraphics.Projectile proj = vgReference.flyingThings.get(i);
            if(!proj.update(vgReference.gravitationalAcceleration)) continue;
            for(int j = 0; j < vgReference.houses.size(); j++)
            {
                VectorGraphics.House h = vgReference.houses.get(j);
                if(h.contains(proj.getPosition()))
                {
                    h.explode();
                    vgReference.houses.remove(j);
                    j--;
                }
            }
        }
        vgReference.repaint();
    }
}
