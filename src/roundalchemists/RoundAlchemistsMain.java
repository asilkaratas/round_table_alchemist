/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roundalchemists;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author asilkaratas
 */
public class RoundAlchemistsMain
{
    public RoundAlchemistsMain()
    {
        RoundTable roundTable = new RoundTable(6);
        roundTable.start();
    }
    
    
}
