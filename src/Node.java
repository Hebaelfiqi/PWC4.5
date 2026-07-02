import java.util.Vector;

//import java.awt.*;
//import java.util.Vector;
public class Node {
	int id; // unique
	String type;
	int parent_id;
	Attribute attribute;  //in case of decision
	String class_label; //in case of leaf
	Double N;
	Double E;
	Double Errors;
	Double Node_Children_Error=0.0;
	boolean all_children_are_leafs=true;
	Vector<Outcome> Node_outcomes=new Vector<Outcome>();

	Vector<Integer> examples_ids= new Vector<Integer>();

	Vector<Tree_Branchs> tree_branchs;
	Vector<Double> weights;


	public Node(String type,int id){
		this.id = id;
		this.type = type;
		tree_branchs=new Vector<Tree_Branchs>();
		weights=new Vector<Double>();
	}

	public Node(){
		tree_branchs=new Vector<Tree_Branchs>();
		weights=new Vector<Double>();
	}





}
