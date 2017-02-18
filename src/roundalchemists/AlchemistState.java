/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roundalchemists;

/**
 *
 * @author asilkaratas
 */
public enum AlchemistState
{
    THINKING("Thinking"),
    WAITING("Waiting"),
    WORKING("Working"),
    GONE("Gone");
    
    private String name;
    
    private AlchemistState(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    
}
