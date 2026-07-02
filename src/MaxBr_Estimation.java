import java.util.Vector;

public final class MaxBr_Estimation {
	private final Vector<Node> new_subtree;
	private final Double Estimated_Error;

	public MaxBr_Estimation (Vector<Node> new_subtree, Double Estimated_Error) {
		this.new_subtree = new_subtree;
		this.Estimated_Error = Estimated_Error;
	}

	public Vector<Node> getNew_subtree() {
		return new_subtree;
	}

	public Double getEstimated_Error() {
		return Estimated_Error;
	}

	@SuppressWarnings("unchecked")
	public static MaxBr_Estimation EstimateError_MaxBr(int Node_id, int max_br)

	{	//all of this function code is wrote based on numerical attribute
		//get all the required data from the parent node

		Node theparent_node=Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(Node_id));
		Double returned_error=0.0;
		Vector<Outcome> Parent_Node_outcomes=theparent_node.Node_outcomes;
		Vector<Integer> Parent_IdS=new Vector<Integer>();
		Vector<String> Parent_classes=new Vector<String>();
		Vector<Double> Parent_weights= new Vector<Double>();
		for (int i=0;i<Parent_Node_outcomes.size();i++)
		{
			Parent_IdS.addAll((Vector<Integer>) Parent_Node_outcomes.get(i).IdS.clone());
			Parent_classes.addAll((Vector<String>) Parent_Node_outcomes.get(i).classes.clone());
			Parent_weights.addAll((Vector<Double>) Parent_Node_outcomes.get(i).weights.clone());
		}
		//get the structure of this branch subtree, and divide the data according to it
		//Node node_to_be_raised =Globals.Pruned_Tree_Nodes.get(Globals.Item_Location[theparent_node.tree_branchs.get(max_br).children_id]);
		Globals.subtree_counter=1;
		int max_br_node_id=theparent_node.tree_branchs.get(max_br).children_id;
		Vector<Node> Subtree=new Vector<Node>();
		Subtree.addAll(Subtree_based_Splitting(max_br_node_id, 0,Parent_IdS,Parent_classes,Parent_weights));
		returned_error=EstimateErrors_Subtree(Subtree,1);
		return new MaxBr_Estimation(Subtree ,returned_error);
	}





	public static Vector<Node> Subtree_based_Splitting(int equivalent_node_id,int parent_id,   Vector<Integer> Ids, Vector<String> classes,Vector<Double> weights)
	{		//all of this function code is wrote based on numerical attribute
		Vector<Node> returned_nodes=new Vector<Node>();

		Node equivalent_node=Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(equivalent_node_id));
		Node new_node=new Node(equivalent_node.type,Globals.subtree_counter);


		if (new_node.type.equals("Decision"))
		{
			new_node.attribute=equivalent_node.attribute;
			/*		if (new_node.attribute.type.equalsIgnoreCase("numeric")) 

			{
			Vector<Vector<Double>> values=new Vector<Vector<Double>>();
			for (int i=0;i<Ids.size();i++)
				{
				values.add(new_node.attribute.getNum_values().get(Ids.get(i)));
				}

			Multiple_Variables result = Multiple_Variables.max_class(classes,weights);
			new_node.class_label=result.getMost_Frequent_class();
			new_node.E= result.getError();
		    new_node.N=Main.calc_sum(weights);
		    new_node.examples_ids=Ids;
		    new_node.weights=weights;
		    new_node.parent_id=parent_id;
		    returned_nodes.add(new_node);

			double equivalent_node_splitpoint=Double.parseDouble(equivalent_node.tree_branchs.get(0).branch_value);

		    Vector<numerical_outcomes> num_freqCi_out=new_node.attribute.num_freqCi(classes,equivalent_node_splitpoint,values, Ids,weights);

		    numerical_outcomes less_outcome=num_freqCi_out.get(0);
		    numerical_outcomes greater_outcome=num_freqCi_out.get(1);

			Double less_than_N=Main.calc_sum(less_outcome.weights);

		    Double greater_N=Main.calc_sum(greater_outcome.weights); 

		    Tree_Branchs less_than_branch=new Tree_Branchs();
		    less_than_branch.branch_operator=equivalent_node.tree_branchs.get(0).branch_operator;
		    less_than_branch.branch_value=equivalent_node.tree_branchs.get(0).branch_value;
		    less_than_branch.branch_factor=less_than_N/(double)(less_than_N+greater_N);
		    Globals.subtree_counter++;
		    less_than_branch.children_id=Globals.subtree_counter;
		    new_node.tree_branchs.add(less_than_branch);

		    Outcome less_outcome_4add=numerica_outcome_2Norm(less_outcome);
		    new_node.Node_outcomes.add(less_outcome_4add);

		    returned_nodes.addAll((Vector<Node>) Subtree_based_Splitting(equivalent_node.tree_branchs.get(0).children_id,new_node.id,less_outcome.IdS,less_outcome.classes,less_outcome.weights));

		    Tree_Branchs greater_branch=new Tree_Branchs();
		    greater_branch.branch_operator=equivalent_node.tree_branchs.get(1).branch_operator;
		    greater_branch.branch_value=equivalent_node.tree_branchs.get(1).branch_value;
		    greater_branch.branch_factor=greater_N/(double)(less_than_N+greater_N);
		    Globals.subtree_counter++;
		    greater_branch.children_id=Globals.subtree_counter;
		    new_node.tree_branchs.add(greater_branch);

		    Outcome greater_outcome_4add=numerica_outcome_2Norm(greater_outcome);
		    new_node.Node_outcomes.add(greater_outcome_4add);

		    returned_nodes.addAll((Vector<Node>) Subtree_based_Splitting(equivalent_node.tree_branchs.get(1).children_id,new_node.id,greater_outcome.IdS,greater_outcome.classes,greater_outcome.weights));

			}

		else //Decision nominal

		{*/
			Multiple_Variables result = Multiple_Variables.max_class(classes,weights);
			new_node.class_label=result.getMost_Frequent_class();
			new_node.E= result.getError();
			new_node.N=PWC45.calc_sum(weights);
			new_node.examples_ids=Ids;
			new_node.weights=weights;
			new_node.parent_id=parent_id;
			returned_nodes.add(new_node);

			Vector<Vector<String>> curr_values= Attribute.get_selected_Attribute_value((Integer[])  PWC45.Integer_Vector_toArray(Ids), new_node.attribute.getAtt_id());

			new_node.attribute.setSelected_values(curr_values);

			new_node.attribute.distinct(PWC45.Integer_Vector_toArray(Ids),new_node.attribute.getSelected_values(),PWC45.String_Vector_toArray(classes),weights);
			Vector<Outcome> outcomes=new_node.attribute.getOutcomes();

			if (Globals.all_possible_branches)
			{
				for (int i=0;i<outcomes.size();i++)
				{
					Outcome this_outcome=outcomes.get(i);
					Tree_Branchs this_branch=new Tree_Branchs();
					this_branch.branch_operator=equivalent_node.tree_branchs.get(i).branch_operator;
					this_branch.branch_value=equivalent_node.tree_branchs.get(i).branch_value;
					this_branch.branch_factor=this_outcome.IdS.size()/(double)(new_node.N);
					Globals.subtree_counter++;
					this_branch.children_id=Globals.subtree_counter;
					new_node.tree_branchs.add(this_branch);
					new_node.Node_outcomes.add(this_outcome);
					// if (this_outcome.IdS.size()!=0)
					// 	{
					returned_nodes.addAll((Vector<Node>) Subtree_based_Splitting(equivalent_node.tree_branchs.get(i).children_id,new_node.id,this_outcome.IdS,this_outcome.classes,this_outcome.weights));

					//				    	}
					//				    else
					//				    	{
					//				    	returned_nodes.add(new_node);
					//				    	}
				}
			}
			else
			{
				for (int i=0;i<outcomes.size();i++)
				{
					Outcome this_outcome=outcomes.get(i);

					if (this_outcome.IdS.size()!=0)
					{
						Tree_Branchs this_branch=new Tree_Branchs();
						this_branch.branch_operator=equivalent_node.tree_branchs.get(i).branch_operator;
						this_branch.branch_value=equivalent_node.tree_branchs.get(i).branch_value;
						this_branch.branch_factor=this_outcome.IdS.size()/(double)(new_node.N);
						Globals.subtree_counter++;
						this_branch.children_id=Globals.subtree_counter;
						new_node.tree_branchs.add(this_branch);
						new_node.Node_outcomes.add(this_outcome);
						returned_nodes.addAll((Vector<Node>) Subtree_based_Splitting(equivalent_node.tree_branchs.get(i).children_id,new_node.id,this_outcome.IdS,this_outcome.classes,this_outcome.weights));
					}


				}
			}
			//}

		}
		else//leaf
		{
			if (Ids.size()==0)
			{
				new_node.class_label=equivalent_node.class_label;
				new_node.E=0.0;
				new_node.N=0.0;
				new_node.examples_ids=Ids;
				new_node.weights=weights;
				new_node.parent_id=parent_id;
				returned_nodes.add(new_node);
			}
			else
			{
				Multiple_Variables result = Multiple_Variables.max_class(classes,weights);
				new_node.class_label=result.getMost_Frequent_class();
				new_node.E= result.getError();
				new_node.N=PWC45.calc_sum(weights);
				new_node.examples_ids=Ids;
				new_node.weights=weights;
				new_node.parent_id=parent_id;
				returned_nodes.add(new_node);
			}
		}

		return returned_nodes;
	}

	public static double EstimateErrors_Subtree(Vector<Node>subTree, int Node_id)
	{	
		double TreeErrors = 0;
		Node this_node=subTree.get(Node_id-1);

		if (this_node.type.equals("Leaf"))
		{
			TreeErrors=this_node.E+PWC45.compute_error(this_node.E, this_node.N);
			this_node.Errors=TreeErrors;

			return TreeErrors; 
		}
		else
		{
			for(int i=0;i<this_node.tree_branchs.size();i++)
			{
				TreeErrors+=EstimateErrors_Subtree(subTree,this_node.tree_branchs.get(i).children_id);
			}
			this_node.Errors=TreeErrors;
			return TreeErrors;
		}
	}


}
