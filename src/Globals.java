//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.Vector;


public class Globals {

	public static final double log2 = 0.69314718055994530942;
	//create a vector of nodes to store the resulted nodes in it
	public static   Vector<Node> Tree_Nodes=new Vector<Node>();
	public static   Vector<Node> Pruned_Tree_Nodes;
	static boolean Prunning=false;
	public static int Node_Id=1;
	public static int No_Classes;
	public static Vector<Vector<String>> All_data=new Vector<Vector<String>>();
	public static Vector<Vector<Vector<String>>> Training_data=new Vector<Vector<Vector<String>>>();
	public static Vector<Vector<Vector<String>>> Testing_data_prunning=new Vector<Vector<Vector<String>>>();
	public static Vector<Integer>Item_Location;
	public static double CF=0.25; 
	public static Vector<String> all_classes=new Vector<String>();
	public static Vector<String> classes=new Vector<String>();
	public static  Vector<String> Testing_classes_prunning=new Vector<String>();
	public static int no_of_attributes;
	public static Vector<Attribute> attribues=new Vector<Attribute>();
	public static int min_inst_4leaf=1;
	public static DecimalFormat df= new DecimalFormat("#.###");
	public static DecimalFormat df2= new DecimalFormat("#.##");
	public static double Epsilon=0.001 ;
	public static int subtree_counter=0;
	public static String[] classes_names;
	public static boolean all_possible_branches=false;


}
