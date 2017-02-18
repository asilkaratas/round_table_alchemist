/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roundalchemists;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asilkaratas
 */
public class Apprentice extends Thread
{
    private RoundTable roundTable;
    
    public Apprentice(RoundTable roundTable)
    {
        this.roundTable = roundTable;
    }
    
    
    public void run()
    {
        Random random = new Random();
        try
        {
            for(int i = 0; i < 10; ++i)
            {
                Thread.sleep(500 + random.nextInt(300));
                roundTable.fillBowls();
            }
        } catch (InterruptedException ex)
        {
            Logger.getLogger(Alchemist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
