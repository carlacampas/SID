import java.util.*;
import java.util.stream.*;

public class Example{

  public static void main(String[] args){
    
    HashSet s = new HashSet();
    s.add(5);
    Boolean res = s.contains(5);
    Integer[] x = new Integer[]{2,3};
    int[] y = new int[]{1,2};
    List a = Arrays.asList(x);
    for(Object z : a) System.out.println(a.size());
    System.out.println(x.length);
    ArrayList<Integer> l = new ArrayList<Integer>(Arrays.asList(new Integer[]{1,2,3}));
    ArrayList<Integer> l2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1,2,3}));
    for(Integer g: l){
    
      System.out.println(g);
    
    }
    
    System.out.println("I assert they are equal. true or false? " + (l.equals(l2)));
    //ArrayList<Integer> b = new ArrayList<Integer>(Arrays.asList(y));
//     for(Integer c : a){
//       System.out.println(c);
//     
//     }
    
    //System.out.println(a.equals(b));
  
  }

}
