/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roundalchemists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author asilkaratas
 */
public class RoundTable
{
    private final Lock lock = new ReentrantLock();
    
    //controls alchemist's access to tools.
    private Condition[] waitAlchemist;
    
    //controls apprentice's access to bowls.
    private Condition waitApprentice;
    
    //keeps number of ingredient in each bowl.
    private int[] bowls;
    
    
    //keeps state of each bowl.
    private BowlState[] bowlState;
    
    //keeps state of each pod.
    private PodState[] podState;
    
    //keeps state of each alchemist.
    private AlchemistState[] alchemistState;
    
    private Alchemist[] alchemists;
    private Apprentice apprentice;
    
    
    //keeps state of each bowl if apprentice is waiting for filling it.
    private ApprenticeState[] apprenticeState;
    
    
    //keeps filled count of bowls. if it is equals to total bowl count, 
    //that means apprentice filled all bowls and free to go back.
    private int filledCount;
   
    
    private int alchemistCount;
    public RoundTable(int alchemistCount)
    {
        this.alchemistCount = alchemistCount;
        if(alchemistCount < 4 || alchemistCount%2 == 1)
        {
            throw new Error("AlchemistCount should be >=4 and even number");
        }
        
        //initializing all necessary variables
        alchemistState = new AlchemistState[alchemistCount];
        waitAlchemist = new Condition[alchemistCount];
        
        for(int i = 0; i < alchemistCount; ++i)
        {
            alchemistState[i] = AlchemistState.THINKING;
            waitAlchemist[i] = lock.newCondition();
        }
        
        int podCount = alchemistCount/2;
        bowls = new int[podCount];
        bowlState = new BowlState[podCount];
        podState = new PodState[podCount];
        apprenticeState = new ApprenticeState[podCount];
        for(int i = 0; i < podCount; ++i)
        {
            bowls[i] = 5;
            bowlState[i] = BowlState.FREE;
            podState[i] = PodState.FREE;
            apprenticeState[i] = ApprenticeState.FILLED;
        }
        
        int bowlId = 0;
        int podId = 0;
        alchemists = new Alchemist[alchemistCount];
        for(int i = 0; i < alchemistCount; ++i)
        {
            if(i == alchemistCount - 1)
            {
                podId = 0;
            }
            
            //bowlId and podId calculated for each alchemist.
            //these information will be used later checking bowl and pod states.
            Alchemist alchemist = new Alchemist(i, this, bowlId, podId);
            System.out.println(alchemist.toString());
            alchemists[i] = alchemist;
            
            if(i % 2 == 0)
            {
                podId ++;
            }
            else
            {
                bowlId ++;
            }
        }
        
        apprentice = new Apprentice(this);
        
        //this condition makes apprentice wait around table until fills all bowls.
        waitApprentice = lock.newCondition();
    }
    
    public void start()
    {
        printFormat("\nSTART");
        for(int i = 0; i < alchemistCount; ++i)
        {
            Alchemist alchemist = alchemists[i];
            alchemist.start();
        }
        
        apprentice.start();
    }
    
    public void takeTools(Alchemist alchemist) throws InterruptedException
    {
        lock.lock();
        
        try
        {
            int alchemistId = alchemist.getAlchemistId();
            int bowlId = alchemist.getBowlId();
            int podId = alchemist.getPodId();
            
            //checks if alchemist's bowl and pod is free to use.
            while(bowlState[bowlId] != BowlState.FREE ||
               podState[podId] != PodState.FREE)
            {
                //if they are not free, alchemist waits.
                alchemistState[alchemistId] = AlchemistState.WAITING;
                printAlchemist(alchemist);
                waitAlchemist[alchemistId].await();
            }
            
            //alchemist takes tools and start to work.
            bowlState[bowlId] = BowlState.BUSSY;
            podState[podId] = PodState.BUSSY;
            alchemistState[alchemistId] = AlchemistState.WORKING;
            printAlchemist(alchemist);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public void putTools(Alchemist alchemist)
    {
        lock.lock();
        try
        {
            int alchemistId = alchemist.getAlchemistId();
            int bowlId = alchemist.getBowlId();
            int podId = alchemist.getPodId();
            
            alchemist.increaseWorkCount();
            
            //alchemist put tools and decrease ingredient count in his bowl.
            //if bowl has no ingredient left, sets it's state to EMPTY.
            //then alchemist start to think.
            bowls[bowlId] --;
            bowlState[bowlId] = bowls[bowlId] == 0 ? BowlState.EMPTY : BowlState.FREE;
            podState[podId] = PodState.FREE;
            alchemistState[alchemistId] = AlchemistState.THINKING;
            printAlchemist(alchemist);
            
            
            //if alchemist done all work, he is free to go.
            if(alchemist.hasDoneAllWork())
            {
                alchemistState[alchemistId] = AlchemistState.GONE;
                printAlchemist(alchemist);
            }
            
            //if apprentice is waiting to fill alchemist's bowl, here he is filling
            //the bowl and signaling to go. Actually fillBowl method could called
            //in apprentice's threat. Since there is no waiting during filling
            //I called it here.
            if(apprenticeState[bowlId] == ApprenticeState.WAITING)
            {
                /*
                fillBowl(bowlId);
                if(filledCount == bowls.length)
                {
                    waitApprentice.signal();
                }
                        */
                waitApprentice.signal();
            }
            
            
            //I look left alchemist's status if he is waiting and his tools are
            //free to use.
            int leftAlchemistId = getNeighborId(alchemistId, -1);
            Alchemist leftAlchemist = alchemists[leftAlchemistId];
            if(alchemistState[leftAlchemistId] == AlchemistState.WAITING &&
               bowlState[leftAlchemist.getBowlId()] == BowlState.FREE && 
               podState[leftAlchemist.getPodId()] == PodState.FREE)
            {
                System.out.println(String.format("Alchemist id:%d leftSignals:%d", alchemistId, leftAlchemistId));
                waitAlchemist[leftAlchemistId].signal();
            }
            
            
            //I look right alchemist's status if he is waiting and his tools are
            //free to use.
            int rightAlchemistId = getNeighborId(alchemistId, 1);
            Alchemist rightAlchemist = alchemists[rightAlchemistId];
            if(alchemistState[rightAlchemistId] == AlchemistState.WAITING &&
               bowlState[rightAlchemist.getBowlId()] == BowlState.FREE && 
               podState[rightAlchemist.getPodId()] == PodState.FREE)
            {
                System.out.println(String.format("Alchemist id:%d rightSignals:%d", alchemistId, rightAlchemistId));
                waitAlchemist[rightAlchemistId].signal();
            }
            
        }
        finally
        {
            System.out.println(String.format("Alchemist id:%d unlock", alchemist.getAlchemistId()));
                
            lock.unlock();
        }
    }
    
    public void fillBowls() throws InterruptedException
    {
        lock.lock();
        try
        {
            printFormat("\nApprentice comes\n");
            
            //apprentice comes and fills all bowls which is currently free to use.
            //if bowl is bussy at the moment, he markes bowl's apprentice state
            //as waiting.
            filledCount = 0;
            
            for(int i = 0; i < bowls.length; ++i)
            {
                apprenticeState[i] = ApprenticeState.NOT_FILLED;
                if(bowlState[i] != BowlState.BUSSY)
                {
                    fillBowl(i);
                    
                }
                else
                {
                    apprenticeState[i] = ApprenticeState.WAITING;
                }
            }
            
            tryToFreeAllBowls();
            
            //apprentice waits until all bowls are filled.
            while(filledCount != bowls.length)
            {
                waitApprentice.await();
                
                tryToFreeAllBowls();
            }
            
            printFormat("\nApprentice goes\n");
        }
        finally
        {
            lock.unlock();
        }
    }
    
    private void tryToFreeAllBowls()
    {
        for(int i = 0; i < alchemists.length; ++i)
        {
            Alchemist alchemist = alchemists[i];
            if(alchemistState[alchemist.getAlchemistId()] == AlchemistState.WAITING &&
                bowlState[alchemist.getBowlId()] == BowlState.FREE && 
                podState[alchemist.getPodId()] == PodState.FREE)
             {
                 System.out.println(String.format("Apprentice: signals:%d", alchemist.getAlchemistId()));
                 waitAlchemist[alchemist.getAlchemistId()].signal();
             }
        }
    }
    
    private void fillBowl(int id)
    {
        printFormat("Apprentice fill bowlId:%d, bowls:%d->5", id, bowls[id]);
        
        bowls[id] = 5;
        bowlState[id] = BowlState.FREE;
        apprenticeState[id] = ApprenticeState.FILLED;
        
        filledCount ++;
    }
    
    private void printAlchemist(Alchemist alchemist)
    {
        int alchemistId = alchemist.getAlchemistId();
        int bowlId = alchemist.getBowlId();
            
        printFormat("%s bowls:%d, state:%s ", alchemist.toString(), bowls[bowlId], alchemistState[alchemistId].getName());
            
    }
    
    private void printFormat(String format, Object... args)
    {
       System.out.println(String.format(format, args));
    }
    
    
    //this function gives me neightbors id in circular table. 
    //for left neightbor's id => neighborCount = -1
    //right neightbor's id => neighborCount = 1
    //for right neightbor's right neightbor's id => neighborCount = 2
    private int getNeighborId(int id, int neigborCount)
    {
        int neigborId =  (alchemistCount + id + neigborCount) % alchemistCount;
        //System.out.println(String.format("neig: i:%d neigCount:%d neig:%d", i, neigCount, neig));
        return neigborId;
    }
    
   
}
