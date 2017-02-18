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
public class Alchemist extends Thread
{
    private int id;
    private RoundTable roundTable;
    private int bowlId;
    private int podId;
    private int workCount;
    private int maxWorkCount;
    
    public Alchemist(int id, RoundTable roundTable, int bowlId, int podId)
    {
        this.id = id;
        this.roundTable = roundTable;
        this.bowlId = bowlId;
        this.podId = podId;
        maxWorkCount = 10;
    }
    
    public int getAlchemistId()
    {
        return id;
    }
    
    public int getBowlId()
    {
        return bowlId;
    }
    
    public int getPodId()
    {
        return podId;
    }
    
    public boolean hasDoneAllWork()
    {
        return workCount == maxWorkCount;
    }
    
    public void increaseWorkCount()
    {
        workCount ++;
    }
    
    public String toString()
    {
        return String.format("Alchemist id:%d, bowlId:%d, podId:%d, workCount:%d", id, bowlId, podId, workCount);
    }
    
    public void run()
    {
        Random random = new Random();
        try
        {
            for(int i = 0; i < maxWorkCount; ++i)
            {
                Thread.sleep(200 + random.nextInt(300));
                roundTable.takeTools(this);
                Thread.sleep(200 + random.nextInt(300));
                roundTable.putTools(this);
            }
            
            
        } catch (InterruptedException ex)
        {
            Logger.getLogger(Alchemist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
