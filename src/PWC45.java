import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;
import java.util.Collections;
import org.apache.commons.cli.*;





public class PWC45 {

	/*
	 * 1- read data 2- handle special cases 3- select best decision-based
	 * attribute 4- create a node for that attribute and calculate its outcomes
	 * 5- create subtree for that attribute
	 */

	enum ATTRIPUTE_TYPE {
		NUMERIC, NOMINAL
	};

	public static void main(String[] args) {

		String DF="DF";
		String path="";
		String output_path="";
		Random rand  = new Random();
		Globals.Prunning=true;
		// choose the testing option
		// 1. using the training data (the default option)
		// 2. using a testing file
		// 3. using cross-over //not implemented yet
		//4. split the data to 1/3 testing 2/3 training

		int testing_option = 1;
		boolean ordered_data=false;


		//handling the arguments

		// create Options object
		Options options = new Options();
		// add t option
		options.addOption("f", true, "Specify the filename stem (default DF)");
		options.addOption("u",false,"Evaluate trees produced on unseen cases in file filestem.test.");
		options.addOption("p",false,"Unprunned tree");
		options.addOption("o",false,"Ordered data that need randomization for class balance");
		options.addOption("r",true,"Specify the seed number for random generator");
		options.addOption("ip",true,"Specify the data input path");
		options.addOption("op",true,"Specify the data output path");
		options.addOption("b",false,"Generate all possible branches for each relationship based split");
		HelpFormatter h = new HelpFormatter();

		try{
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);
			//@SuppressWarnings("unused")
			if ( cmd.hasOption("f") )  
			{  
				DF=cmd.getOptionValue("f");  
			}

			if (cmd.hasOption("u"))
			{testing_option=2;}

			if (cmd.hasOption("p"))
			{Globals.Prunning=false;}

			if (cmd.hasOption("o"))
			{ordered_data=true;}

			if (cmd.hasOption("b"))
			{Globals.all_possible_branches=true;}

			if (cmd.hasOption("r"))
			{rand = new Random(Integer.parseInt(cmd.getOptionValue("r")));}

			if ( cmd.hasOption("ip") )  
			{  
				path=cmd.getOptionValue("ip");
				if ( cmd.hasOption("op") )  
				{  
					output_path=cmd.getOptionValue("op");  
				}
				else
				{
					output_path=cmd.getOptionValue("ip");
				}

			} 

			// 1- read file
			try {

				String names_file=DF+".names";
				String data_file=DF+".data";
				String test_file=DF+".test";

				// define the random seed one form the following list	{42,61, 13, 3, 82,27,19,47,93,2};
				//Random rand = new Random(42);



				// set the minimum accepted number of instances for a leaf
				Globals.min_inst_4leaf = 2;

				//define a boolean variable that will stop the program if it has any nominal attribute,
				//as the pairing cannot be handled with nominal attributes
				boolean STOP=false;


				//read the names file
				BufferedReader names_file_reader = new BufferedReader(new FileReader (path+ names_file));
				String names_row = names_file_reader.readLine();
				//skip comment and empty lines
				while ((names_row.startsWith("|") == true)||names_row.isEmpty()) {
					System.out.println(names_row);
					names_row = names_file_reader.readLine();
				}
				//the first readable line is the classes
				String classes_row=names_row;
				Vector<String> attributes_definition= new Vector<String>();
				names_row = names_file_reader.readLine();
				while (names_row !=null){
					// Again skip any other comment and empty lines
					while ((names_row.startsWith("|") == true)||names_row.isEmpty()) {
						System.out.println(names_row);
						names_row = names_file_reader.readLine();
					}
					//the next readable line is an variable/attribute
					attributes_definition.add(names_row);
					//names_row = names_file_reader.readLine();
					String[]  attributes_definition_split= names_row.split(":");
					Attribute att = new Attribute();
					att.setName(attributes_definition_split[0]);
					att.setAtt_id(attributes_definition.size()-1);
					if (attributes_definition_split[1].contains("continuous.")) //numerical attribute
					{
						att.setType("numeric");
						names_row = names_file_reader.readLine();
					}
					else //nominal attribute
					{
						STOP=true;
						att.setType("nominal");
						Vector<Outcome> nominal_att_outcomes=new Vector<Outcome>();
						String[] nominal_att_values= attributes_definition_split[1].split(",");
						nominal_att_values[nominal_att_values.length-1]=nominal_att_values[nominal_att_values.length-1].substring(0, nominal_att_values[nominal_att_values.length-1].length()-1);
						for (int i=0;i<nominal_att_values.length;i++)
						{
							Outcome o = new Outcome();
							o.value = nominal_att_values[i].replaceAll("\\s","");
							nominal_att_outcomes.add(o);
						}
						att.setOutcomes(nominal_att_outcomes);
						names_row = names_file_reader.readLine();
					}
					Globals.attribues.add(att);

				}

				//if any of the variables are nominal , then stop and exit the program
				if (STOP)
				{System.out.println("Relationships in PWC4.5 are based on numerical attributes ");
				System.out.println("The provided dataset contains one or more nominal attributes ");
				System.exit(0);
				}



				Globals.no_of_attributes = attributes_definition.size();

				// All numerical attributes will be handled through relationship only.
				// Pairs will be labeled with their relationships, then outcomes will be calculated as for nominals
				// Therefore, all attribute types will be changed into nominal

				for (int i=0;i<Globals.no_of_attributes ;i++) 
				{
					Globals.attribues.get(i).setType("nominal");
				}

				BufferedReader data_file_reader = new BufferedReader(new FileReader (path+ data_file));

				String dataRow = data_file_reader.readLine();

				if (testing_option==4)
				{
					//read into all_data all lines

					while (dataRow != null) {

						String[] dataArray = dataRow.split(",");
						Vector<String> new_instance = new Vector<String>();
						for (int i = 0; i < Globals.no_of_attributes; i++) {
							new_instance.add(dataArray[i]);
						}

						Globals.All_data.add(new_instance);
						Globals.all_classes.add(dataArray[Globals.no_of_attributes]);
						dataRow = data_file_reader.readLine();
					}
					// write a function to split the data into training and testing
					Split_for_testing();

				}
				else
				{
					while (dataRow != null) {

						String dataRow2 = data_file_reader.readLine();
						Vector<Vector<String>> PS_i =new Vector<Vector<String>>() ;

						String[] dataArray = dataRow.split(",");
						//read the pair in the same time

						String[] dataArray2 = dataRow2.split(",");


						String[] p1= new String[dataArray.length];
						String[] p2= new String[dataArray.length];
						double rand_no=rand.nextDouble();// this function Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.

						if (ordered_data)
						{
							if (rand_no<0.5) //if rand<0.5 then first dataArray is p1 , else dataArray2 is p1
							{
								p1=dataArray;
								p2=dataArray2;
							}
							else
							{
								p1=dataArray2;
								p2=dataArray;
							}
						}
						else
						{
							p1=dataArray;
							p2=dataArray2;
						}

						for (int i = 0; i < Globals.no_of_attributes; i++) {
							// define  V_i vector 
							Vector<String> V_i= new Vector<String>();
							V_i.add(p1[i]);
							V_i.add(p2[i]);
							PS_i.add(V_i);
						}

						Globals.Training_data.add(PS_i);

						//To define the class of the pair
						String C1=p1[Globals.no_of_attributes];
						String C2=p2[Globals.no_of_attributes];
						//			        AttributedString astr = new AttributedString("p1");  
						//			        astr.addAttribute(TextAttribute.SUPERSCRIPT,TextAttribute.SUPERSCRIPT_SUB, 1, 2);
						//			        AttributedString astr2 = new AttributedString("p2");  
						//			        astr2.addAttribute(TextAttribute.SUPERSCRIPT,TextAttribute.SUPERSCRIPT_SUB, 1, 2);
						//			        
						//					Globals.classes.add((astr+"\u2192"+C1+astr2+"\u2192"+C2));




						Globals.classes.add(("p1\u2192"+C1+", p2\u2192"+C2));
						//Globals.classes.add(("p\u2081"+" \u2192 "+C1+", "+"p\u2082"+" \u2192 "+C2));
						dataRow = data_file_reader.readLine();

					}

				}
				data_file_reader.close();




				// define the Ids that will be passed to the composeSubTree
				// //initially all of the data
				Integer[] all_ids = new Integer[Globals.Training_data.size()];
				Vector<Double> all_weights = new Vector<Double>();
				for (int i = 0; i < Globals.Training_data.size(); i++) {
					all_ids[i] = i;
					all_weights.add((double) 1);
				}

				//Globals.No_Classes=get_countof_classes(Globals.classes);

				Globals.No_Classes=  (int)  (classes_row.split(",").length);
				Globals.classes_names=new  String[Globals.No_Classes];
				Globals.classes_names=classes_row.split(",");

				// set attributes type

				for (int i = 0; i < Globals.no_of_attributes; i++) {
					Vector<Vector<String>> values = new Vector<Vector<String>>();
					values = get_all_Attribute_value(i);
					Globals.attribues.get(i).setAll_values(values);
					// set values of attribute

					boolean first_att_is_missing=true;
					int first_data_loc=0;
					Vector<String> first_att_val=new Vector<String> ();
					while (first_att_is_missing==true)
					{
						first_att_val = Globals.Training_data.get(first_data_loc).get(i);
						if (! first_att_val.get(0).equals("?"))
						{
							first_att_is_missing=false;
							//first_att_val=Globals.Training_data.get(first_data_loc).get(i);
						}
						first_data_loc++;

					}
					// set some attribute values based on its type
					//				if (Globals.attribues.get(i).equals("numeric")) {
					//					// Globals.attribues.get(i).
					//					Globals.attribues.get(i).setNum_values(convert_Str2double(Globals.attribues.get(i).getAll_values(), i));
					//					if (Globals.attribues.get(i).isMissing_vals_exists() == true) {
					//						for (int j = 0; j < Globals.attribues.get(i).missing_vals.IdS.size(); j++) {
					//							Globals.attribues.get(i).missing_vals.classes.add(Globals.classes.get(Globals.attribues.get(i).missing_vals.IdS.get(j)));
					//							Globals.attribues.get(i).missing_vals.weights.add(all_weights.get(Globals.attribues.get(i).missing_vals.IdS.get(j)));
					//						}
					//					}
					//
					//					double[] sorted_num_values = Arrays.copyOf(	Globals.attribues.get(i).getNum_values(), Globals.attribues.get(i).getNum_values().length); 
					//					// create copy of the values to be sorted
					//
					//					Arrays.sort(sorted_num_values);
					//					Globals.attribues.get(i).setSorted_values(sorted_num_values);
					//
					//				} else {
					//					//call a function that will generate a struct array of all possible outcomes based on the training data
					//					//	Globals.attribues.get(i).setOutcomes(get_distinct_outcomes(Globals.attribues.get(i).getAll_values()));
					//				}

				}



				// call the main function, send the parent id as 0 which means root node
				composeSubTree(all_ids, 0, null, all_weights);
				//calculate children error for each node
				// 2. Create ouput file for unpruned
				write_output_files(output_path,DF);


				if (Globals.Prunning)
				{

					Tree_Prunning();
					Update_Item_Location_intheNodes(Globals.Pruned_Tree_Nodes, Globals.Item_Location);
					write_output_files_pruned(output_path,DF);
				}


				// 3. Testing

				switch (testing_option) {
				case 1:// using the training data
					testing_using_trainingdata(output_path);
					break;

				case 2: // using a testing file

					testing_using_file(test_file,path,rand,ordered_data,output_path);
					break;

				case 3:
					break;
				}

			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}

		} catch (ParseException e) { System.err.println("Wrong parameters:" + e.getMessage());
		h.printHelp("help", options);
		System.exit(1);
		}
	}

	public static  Vector<String> get_classes(Vector<String> classes)
	{
		Vector<String> disticive_classes = new Vector<String>();
		disticive_classes.add(classes.get(0));
		for (int i = 1; i < classes.size(); i++) {
			boolean exist = true;
			String Ci = classes.get(i);
			for (int j = 0; j < disticive_classes.size(); j++) {
				String comparingItem = disticive_classes.get(j);
				if (Ci.equalsIgnoreCase(comparingItem)) {
					exist = true;
					break;
				} else {
					exist = false;
				}

			}
			if (!exist) {
				disticive_classes.add(Ci);
			}
		}
		return disticive_classes ;

	}




	public static void write_output_files(String output_path,String inputfilename) {
		FileWriter fstream;
		try {
			fstream = new FileWriter(output_path+inputfilename+"_unpruned_out.csv");
			BufferedWriter f_out = new BufferedWriter(fstream);
			String line_sep = System.getProperty("line.separator"); // newline
			f_out.write("Id,parent,type,attribute,tree branch cond,tree branch value, child_node id,class");
			f_out.write(line_sep);
			for (int i = 0; i < Globals.Tree_Nodes.size(); i++) {
				f_out.write(Globals.Tree_Nodes.get(i).id + " , ");
				f_out.write(Globals.Tree_Nodes.get(i).parent_id + " , ");
				f_out.write(Globals.Tree_Nodes.get(i).type + " , ");
				String attribute_val;
				try {
					attribute_val = Globals.Tree_Nodes.get(i).attribute.getName();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					attribute_val = " ";
				}

				f_out.write(attribute_val + " , ");

				// f_out.write( line_sep);
				if (Globals.Tree_Nodes.get(i).type == "Decision") {
					f_out.write(Globals.Tree_Nodes.get(i).tree_branchs.get(0).branch_operator + " , ");
					f_out.write(Globals.Tree_Nodes.get(i).tree_branchs.get(0).branch_value + " , ");
					f_out.write(Globals.Tree_Nodes.get(i).tree_branchs.get(0).children_id + ",");
					f_out.write(line_sep);
					for (int j = 1; j < Globals.Tree_Nodes.get(i).tree_branchs.size(); j++) 
					{
						f_out.write(",,,,");
						f_out.write(Globals.Tree_Nodes.get(i).tree_branchs.get(j).branch_operator + " , ");
						f_out.write(Globals.Tree_Nodes.get(i).tree_branchs.get(j).branch_value + " , ");
						f_out.write(Globals.Tree_Nodes.get(i).tree_branchs.get(j).children_id + ",");
						f_out.write(line_sep);
					}
				} else // leaf
				{
					f_out.write(",,," + Globals.Tree_Nodes.get(i).class_label+ ",");
				}
				f_out.write(line_sep);
			}

			// Close the output stream
			f_out.close();

			// Drawing
			// Graphics g = null;
			// Graphics2D g2d = (Graphics2D)g;
			Writer dc_rules = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output_path+"Unpruned_"+inputfilename+ "_decision_rules.txt"), "UTF-8"));

			// writing if-then decision file
			//			FileWriter fstream2 = new FileWriter(output_path+"Unpruned_"+inputfilename+ "_decision_rules.txt");
			//			BufferedWriter dc_rules = new BufferedWriter(fstream2);
			// for (int i=1; i<=Globals.Tree_Nodes.size();i++)
			dc_rules.write("PWC4.5 Unpruned Tree");
			dc_rules.write("\r\n");
			dc_rules.write("__________________");
			dc_rules.write("\r\n");
			dc_rules.write(write_decision_rules_v2(1, 0));// pass the first node to start with , and level 0 for writing nested if statements

			int number_of_leafs = 0;
			for (int i = 0; i < Globals.Tree_Nodes.size(); i++) {
				if (Globals.Tree_Nodes.get(i).type == "Leaf") {
					number_of_leafs++;

				}

			}
			dc_rules.write("Number of Leaves:  " + number_of_leafs + line_sep);

			dc_rules.write("Size of the tree : " + Globals.Tree_Nodes.size());

			// }
			dc_rules.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void testing_using_file(String testing_fn,String path,Random rand,boolean ordered_data, String output_path) {
		Vector<String> actual_class = new Vector<String>();
		Vector<String> predicted_class = new Vector<String>();

		try {

			BufferedReader Testing_File = new BufferedReader(new FileReader(path+
					testing_fn));
			String dataRow1;
			String dataRow2;
			try {
				//				dataRow = Testing_File.readLine();
				//				String[] headers = dataRow.split(",");
				//				boolean match = true;
				//
				//				// check for the order of attributes
				//				for (int i = 0; i < Globals.no_of_attributes; i++) {
				//					Attribute att = Globals.attribues.get(i);
				//					String att_name = att.getName();
				//					if (att_name.equals(headers[i]))
				//					{	match = match & true;
				//					} else
				//					{	match = match & false;
				//					}
				//
				//				}
				//				if (match == false) {
				//					System.out.println("Attributes in testing file doesn't match the training file");
				//					return;
				//				} else {
				dataRow1 = Testing_File.readLine();


				// set headers
				// String[] dataArray = dataRow.split(",");
				while (dataRow1 != null) {
					dataRow2 = Testing_File.readLine();
					String[] dataArray1 = dataRow1.split(",");
					String[] dataArray2 = dataRow2.split(",");

					String[] p1= new String[dataArray1.length];
					String[] p2= new String[dataArray2.length];
					double rand_no=rand.nextDouble();// this function Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.

					if (ordered_data)
					{
						if (rand_no<0.5) //if rand<0.5 then first dataArray is p1 , else dataArray2 is p1
						{
							p1=dataArray1;
							p2=dataArray2;
						}
						else
						{
							p1=dataArray2;
							p2=dataArray1;
						}
					}
					else
					{
						p1=dataArray1;
						p2=dataArray2;
					}

					Vector<Vector<String>> new_instance= new Vector<Vector<String>>();
					for (int i = 0; i < Globals.no_of_attributes; i++) {
						Vector<String> vi = new Vector<String>();
						vi.add(p1[i]);
						vi.add(p2[i]);
						new_instance.add(vi);
					}


					actual_class.add("p1\u2192"+p1[p1.length - 1]+", "+"p2\u2192"+p2[p2.length - 1]);
					//actual_class.add("p\u2081"+" \u2192 "+p1[p1.length - 1]+", "+"p\u2082"+" \u2192 "+p2[p2.length - 1]);
					String class_output = find_leaf(new_instance, 1);
					predicted_class.add(class_output);
					dataRow1 = Testing_File.readLine();

				}
				Testing_File.close();
				//	}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// set headers

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileWriter fstream;
		try {
			fstream = new FileWriter(output_path+"output_of_"+testing_fn.substring(0,testing_fn.length()-5)+".csv");
			double accuracy = 0;
			BufferedWriter f_out = new BufferedWriter(fstream);
			String line_sep = System.getProperty("line.separator"); // newline
			f_out.write("Atcual,,Predicted" + line_sep);
			for (int i = 0; i < actual_class.size(); i++) {
				f_out.write(actual_class.get(i) + "," + predicted_class.get(i)
						+ line_sep);
				if (actual_class.get(i).equals(predicted_class.get(i))) {
					accuracy++;
				}
			}
			accuracy = accuracy / (double) actual_class.size() * 100;
			//System.out.println("Accuracy= " + accuracy + "%");
			System.out.print(accuracy + "%");
			f_out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static String write_decision_rules(int index) {
		String out = "";

		int space_length = 5;
		Node n = Globals.Tree_Nodes.get(index - 1);

		if (n.type == "Decision") {
			for (int j = 0; j < n.tree_branchs.size(); j++) {
				// out=out+"If (";
				out = out + "\r\n";
				// out=out+"else If (";
				// space_length=space_length+5;
				int level = 1 + n.parent_id;
				space_length = 5 * level;
				String Cond = "(" + n.attribute.getName() + " "
						+ n.tree_branchs.get(j).branch_operator + "  "
						+ n.tree_branchs.get(j).branch_value + " )";
				if (j == 0) {
					out = out + String.format("%" + space_length + "s", "")
							+ "If " + Cond + " then ";
				} else {
					out = out + String.format("%" + space_length + "s", "")
							+ "Else If " + Cond + " then ";
				}
				out = out
						+ write_decision_rules(n.tree_branchs.get(j).children_id);
				// out = out+ n.tree_branchs.get(j).children_id+" " ;
				// out = out+"\r\n";
				// return out;
			}
		} else {
			Integer[] selected_examples = PWC45	.Integer_Vector_toArray(n.examples_ids);
			Vector<String> classes = getClasses(selected_examples);

			// *test if all the instances belongs to the same class or majority
			// class



			Pair<Vector<String>,Vector<Double>> classes_freq= PWC45.freqCi(classes, selected_examples,	n.weights); // return a list contains frequency of each

			Double[] freqCi_S =classes_freq.getSecond().toArray(new Double[classes_freq.getSecond().size()]);

			// class
			out = n.class_label;
			if (freqCi_S.length == 1) {
				out = out + " (" + roundTwoDecimals(freqCi_S[0]) + ")";
			} else {
				Double sum = freqCi_S[0] + freqCi_S[1];
				if (freqCi_S[0] > freqCi_S[1])// write the majority class first
				{
					out = out + " (" + roundTwoDecimals(sum) + "/"
							+ roundTwoDecimals(freqCi_S[1]) + ")";
				} else {
					out = out + " (" + roundTwoDecimals(sum) + "/"
							+ roundTwoDecimals(freqCi_S[0]) + ")";
				}

			}
			out = out + "\r\n";
		}

		return out;

	}

	@SuppressWarnings("unchecked")
	public static Vector<Examples> composeSubTree(Integer[] selected_examples, int p_id, Tree_Branchs causing_branch, Vector<Double> weights) {
		//	test for empty selected_examples: if empty, then the class label of this branch is the class label for parent node.

		if (selected_examples.length==0)
		{
			Node single_class_node = new Node("Leaf", Globals.Node_Id);
			single_class_node.class_label = Globals.Tree_Nodes.get(p_id-1).class_label;
			single_class_node.parent_id = p_id;
			//single_class_node.weights = weights;
			double N=0;
			single_class_node.N=N;
			single_class_node.E=0.0;
			//single_class_node.Node_Error = Main.compute_error(0, N);  //the return is N * U(E,N)
			//as it is a leaf node and there is no children , then Node_Children_Error=0
			//single_class_node.Node_Children_Error=0.0;

			if (p_id != 0) {
				causing_branch.children_id = Globals.Node_Id;
			}

			Globals.Node_Id++;
			Globals.Tree_Nodes.add(single_class_node);

		}

		else
		{

			// 1. create a root node for the tree

			// 2. handle special case
			// Vector<Vector<String>> examples=ex1.getExamples();
			// create attributes

			Vector<String> classes = getClasses(selected_examples);
			// Attribute att= Globals.attribues.get((causing_branch.attribue_id));
			// *test if all positive or negative
			Pair<Vector<String>,Vector<Double>> classes_freq= PWC45.freqCi(classes, selected_examples, weights);// return a list contains frequency of each class
			Double[] freqCi_S =classes_freq.getSecond().toArray(new Double[classes_freq.getSecond().size()]);

			// find the best class



			boolean leaf = false;
			if ((freqCi_S.length == 1)||(selected_examples.length<2*Globals.min_inst_4leaf))
			{
				leaf = true;
			}

			Double best_class_freq=0.0;
			int best_class=0;
			String[] loc_classes_names=Globals.classes_names;  //initialize best_class

			String selected_class="p1\u2192"+loc_classes_names[0]+", p2\u2192"+loc_classes_names[1].substring(0,loc_classes_names[1].length()-1);

			//String selected_class="p\u2081"+" \u2192 "+loc_classes_names[0]+", "+"p\u2082"+" \u2192 "+loc_classes_names[1].substring(0,loc_classes_names[1].length()-1);

			if (leaf == true) {// generate a leaf node with this class
				// any element will be OK as they are all the same
				//in the case of equality, where the first class will be used, 
				//this class will be the class of the first class in the names file


				for (int i=0;i<freqCi_S.length;i++) //find its location and frequency
				{
					if (classes_freq.getFirst().get(i).equals(selected_class))
					{
						best_class_freq=classes_freq.getSecond().get(i);
						best_class=i;
						break;
					}
				}
				for (int i=0;i<freqCi_S.length;i++)
				{
					if (freqCi_S[i]>freqCi_S[best_class]	)
					{
						best_class=i;
					}
				}

				selected_class=classes_freq.getFirst().get(best_class);
				best_class_freq = freqCi_S[best_class];


				Node single_class_node = new Node("Leaf", Globals.Node_Id);
				single_class_node.class_label = selected_class;
				single_class_node.parent_id = p_id;
				single_class_node.weights = weights;
				double N=PWC45.calc_sum(weights);
				single_class_node.N=N;
				single_class_node.E=N-best_class_freq;
				//single_class_node.Node_Error=Main.compute_error(single_class_node.E, N);  //the return is N * U(E,N)
				//as it is a leaf node and there is no children , then Node_Children_Error=0
				single_class_node.Node_Children_Error=0.0;

				Vector<Integer> leaf_ids = new Vector<Integer>();
				for (int i = 0; i < selected_examples.length; i++) {
					leaf_ids.add(selected_examples[i]);
				}

				single_class_node.examples_ids = leaf_ids;

				if (p_id != 0) {
					causing_branch.children_id = Globals.Node_Id;
				}

				Globals.Node_Id++;
				Globals.Tree_Nodes.add(single_class_node);

			}

			else if (Globals.Training_data.get(0).size() == 1) {
				// generate a leaf node that has the most classes
				// loop on the integer list that has the frequencies to return the
				// class with the maximum counts
				Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
				selected_class= result.getMost_Frequent_class(); 
				Double E= result.getError();
				Double N=PWC45.calc_sum(weights);
				//selected_class = max_class(classes);

				Node single_class_node = new Node("Leaf", Globals.Node_Id);
				single_class_node.N=N;
				single_class_node.E=E;
				//single_class_node.Node_Error=E+Main.compute_error(E, N);

				//as it is a leaf node and there is no children , then Node_Children_Error=0
				single_class_node.Node_Children_Error=0.0;

				if (p_id != 0) {
					causing_branch.children_id = Globals.Node_Id;
				}
				single_class_node.parent_id = p_id;
				single_class_node.weights = weights;
				single_class_node.examples_ids = IntegerofArray_toVector(selected_examples);
				Globals.Node_Id++;
				single_class_node.class_label = selected_class;
				Globals.Tree_Nodes.add(single_class_node);
			}
			//		 else if ((freqCi_S.length==2) & (freqCi_S[0]<Globals.min_inst_4leaf | freqCi_S[1]<Globals.min_inst_4leaf )) 
			//			 //check if any of the classes has only one instance, in this case it is a leaf node with majority class
			//		 {
			//		
			//		 //generate a leaf node that contains the majority class
			//		 selected_class= max_class(classes);
			//		 Node single_class_node=new Node("Leaf",Globals.Node_Id);
			//		 if(p_id!=0){causing_branch.children_id=Globals.Node_Id;}
			//		 single_class_node.parent_id=p_id;
			//		 single_class_node.weights=weights;
			//		 single_class_node.examples_ids=IntegerofArray_toVector(selected_examples);
			//		 Globals.Node_Id++;
			//		 single_class_node.class_label=selected_class;
			//		 Globals.Tree_Nodes.add(single_class_node);
			//		 }
			//

			else {
				// 3. otherwise begin
				// find the best attribute A
				// compute info(T)
				double info_T = Entropy(freqCi_S);
				double sum_gain = 0;
				int count_gain_avg = 0;
				double max_gain_ratio = 0;
				Attribute selected_att = new Attribute();
				//double parent_gain = 0;

				//if (p_id > 2) {
				// int grand_parent= Globals.Tree_Nodes.get(p_id-1).parent_id;
				//	parent_gain = Globals.Tree_Nodes.get(p_id - 1).attribute.getGain();

				//	}
				//	System.out.println(selected_examples.length);
				for (int i = 0; i < Globals.no_of_attributes; i++) // loop on the
					// attributes
				{

					// set outcomes
					String[] temp_classes_arr=classes.toArray(new String[classes.size()]);

					Vector<Double> clone_weights = (Vector<Double>) weights.clone();
					Globals.attribues.get(i).computeOutcomes(selected_examples,temp_classes_arr, info_T,clone_weights);

					int no_outcome;
					//					if (Globals.attribues.get(i).getType().equalsIgnoreCase("numeric"))
					//					{ if (Globals.attribues.get(i).getGain()==-Globals.Epsilon)
					//					{no_outcome=0;}
					//					else{
					//						no_outcome=Globals.attribues.get(i).getNum_outcomes().size();
					//					}
					//					}
					//					else
					//	{
					no_outcome=Globals.attribues.get(i).getOutcomes().size();
					//	}

					if ((Globals.attribues.get(i).getGain() > -Globals.Epsilon )&& (no_outcome <0.3*Globals.Training_data.size()))
					{
						count_gain_avg++;  //possible
						sum_gain = sum_gain + Globals.attribues.get(i).getGain();
					}

					//System.out.println("Selected Attribute ");

					//	System.out.print("Attribute "+Globals.attribues.get(i).getName());
					/*	if (Globals.attribues.get(i).getType().equals("numeric"))
					{
						System.out.print(" cut="+	 Globals.df.format(Globals.attribues.get(i).getCut_point()));
						}
					System.out.println(", Split_InfoxT= "+Globals.df.format(Globals.attribues.get(i).getSplit_infox())+", Gain= "+Globals.df.format(Globals.attribues.get(i).getGain())+", Gain Ratio= "+Globals.df.format(Globals.attribues.get(i).getGain_ratio()));
					 */
					//System.out.println();
				}

				if (count_gain_avg==0)//then no sensible split attribute
				{
					Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
					selected_class= result.getMost_Frequent_class(); 
					Double E= result.getError();
					Double N=PWC45.calc_sum(weights);
					//selected_class = max_class(classes);

					Node single_class_node = new Node("Leaf", Globals.Node_Id);
					single_class_node.N=N;
					single_class_node.E=E;
					//single_class_node.Node_Error=E+Main.compute_error(E, N);

					//as it is a leaf node and there is no children , then Node_Children_Error=0
					single_class_node.Node_Children_Error=0.0;

					if (p_id != 0) {
						causing_branch.children_id = Globals.Node_Id;
					}
					single_class_node.parent_id = p_id;
					single_class_node.weights = weights;
					single_class_node.examples_ids = IntegerofArray_toVector(selected_examples);
					Globals.Node_Id++;
					single_class_node.class_label = selected_class;
					Globals.Tree_Nodes.add(single_class_node);

				}
				else
				{

					double average_gain;
					if (count_gain_avg>0)
					{average_gain=sum_gain / count_gain_avg;}
					else
					{average_gain=10e6;}




					//to find the best attribute
					max_gain_ratio = -Globals.Epsilon;


					for (int i = 0; i < Globals.no_of_attributes; i++) 
					{float val= (float) -Globals.Epsilon;
					if (Globals.attribues.get(i).getGain() > -Globals.Epsilon)
					{
						val= PWC45.worth(Globals.attribues.get(i).getSplit_infox(),Globals.attribues.get(i).getGain() ,average_gain); //pass (Gain{att],Info[att],avgain)
						Globals.attribues.get(i).setGain_ratio(val);
					}
					if (val>max_gain_ratio)
					{
						max_gain_ratio = val;
						selected_att = Globals.attribues.get(i);
					}

					}


					// the decision attribute for this node is attribute A
					//					System.out.println("Average gain " +average_gain);
					//					System.out.println("Selected Attribute is "+ selected_att.getName());



					// Branching:
					// if this attribute contains missing value
					//					if ((selected_att.isMissing_vals_exists() == true)& selected_att.missing_vals != null) 
					//					{
					//						Outcome missing_val = selected_att.getMissing_vals();
					//						Vector<Integer> missing_val_index = missing_val.IdS;
					//
					//						if (selected_att.getType() == "nominal") {//nominal+missing
					//							Vector<Outcome> outcomes = (Vector<Outcome>) Get_V_Outcomes(selected_att.getOutcomes()); // outcomes in case of nominal
					//							Vector<Integer[]> selected_Ids_arr=new Vector<Integer[]>();
					//							Vector<Tree_Branchs> new_branch = new Vector<Tree_Branchs>();
					//							double Error_if_split=0;
					//							Vector<Vector<Double>> selected_weights=new Vector<Vector<Double>>();
					//							// for each possible value vi of Ai
					//							for (int i = 0; i < outcomes.size(); i++) {
					//								// add new tree branch below root, corresponding to
					//								// the test A=vi
					//								Tree_Branchs new_branch1 = new Tree_Branchs();
					//								new_branch1.branch_value = outcomes.get(i).value;
					//								new_branch1.branch_operator = "=";
					//								Vector<Integer> selected_Ids = outcomes.get(i).IdS;
					//								new_branch1.branch_factor=selected_Ids.size()/(double)(selected_examples.length - missing_val.IdS.size());
					//								new_branch.add(new_branch1);
					//
					//								selected_weights.add((Vector<Double>) outcomes.get(i).weights.clone());
					//								Vector<Double> missing_weights = (Vector<Double>) missing_val.weights.clone();//
					//
					//								double total_number_cases_in_outcomes = calc_sum(weights)- calc_sum(missing_weights);//
					//								Double single_weight = calc_sum(outcomes.get(i).weights) / (double) total_number_cases_in_outcomes;//
					//
					//								selected_Ids.addAll(missing_val_index);//
					//								selected_Ids_arr.add( Integer_Vector_toArray(selected_Ids));
					//
					//								for (int j = 0; j < missing_weights.size(); j++)//
					//								{
					//									missing_weights.set(j,	missing_val.weights.get(j)	* single_weight);
					//								}//
					//
					//								selected_weights.get(i).addAll(missing_weights);//
					//							}
					//
					//							Node decision_node = new Node("Decision", Globals.Node_Id);
					//							decision_node.attribute = selected_att;
					//							decision_node.Node_outcomes=(Vector<Outcome>) Get_V_Outcomes(selected_att.getOutcomes());
					//							decision_node.weights = weights;
					//
					//							Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
					//							decision_node.class_label= result.getMost_Frequent_class(); 
					//							Double E= result.getError();
					//							Double N=Main.calc_sum(weights);
					//							decision_node.N=N;
					//							decision_node.E=E;
					//							decision_node.examples_ids=IntegerofArray_toVector(selected_examples);		
					//
					//
					//							decision_node.parent_id = p_id;
					//							if (p_id != 0) {
					//								causing_branch.children_id = Globals.Node_Id;
					//							}
					//							Globals.Tree_Nodes.add(decision_node);
					//							Globals.Node_Id++;
					//
					//							for (int j = 0; j < outcomes.size(); j++) {
					//								Globals.Tree_Nodes.get(decision_node.id - 1).tree_branchs.add(new_branch.get(j));
					//								composeSubTree((Integer[])selected_Ids_arr.get(j).clone(), decision_node.id, new_branch.get(j), (Vector<Double>)selected_weights.get(j).clone());
					//								if (Globals.Tree_Nodes.get(new_branch.get(j).children_id-1).type.equals("Leaf"))
					//								{Error_if_split+=Globals.Tree_Nodes.get(new_branch.get(j).children_id-1).E;
					//								}
					//								else
					//								{Error_if_split+=Globals.Tree_Nodes.get(new_branch.get(j).children_id-1).Node_Children_Error;  //for parent node
					//								}
					//							}
					//							if (Error_if_split>= E-Globals.Epsilon) //see whether we would have been no worse off with a leaf
					//							{ // collapse tree to a leaf
					//								//System.out.println("Collapse the tree to a leaf node at Node id"+Globals.Node_Id);
					//								decision_node.type="Leaf";
					//								decision_node.Node_Children_Error=(double) 0;
					//								for (int k=decision_node.tree_branchs.size()-1;k>=0;k--)
					//								{
					//									Globals.Tree_Nodes.remove(new_branch.get(k).children_id-1);
					//									Globals.Node_Id--;
					//									decision_node.tree_branchs.remove(k);
					//									//	decision_node.examples_ids.addAll(outcomes.get(k).IdS);
					//								}
					//
					//							}
					//							else
					//							{//decided to keep the split, then update the error E to be the error from splitting
					//								Globals.Tree_Nodes.get(decision_node.id - 1).Node_Children_Error=Error_if_split;
					//							}
					//							/*	
					//						}
					//						else  //do_not_split=true, then it is a leaf
					//						{
					//							Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
					//						}
					//						selected_class= result.getMost_Frequent_class(); 
					//					    Double E= result.getError();
					//					    Double N=Main.calc_sum(weights);
					//						//selected_class = max_class(classes);
					//
					//						Node single_class_node = new Node("Leaf", Globals.Node_Id);
					//						single_class_node.N=N;
					//						single_class_node.E=E;
					//						single_class_node.Node_Error=E+Main.compute_error(E, N);
					//
					//
					//						if (p_id != 0) {
					//							causing_branch.children_id = Globals.Node_Id;
					//						}
					//						single_class_node.parent_id = p_id;
					//						single_class_node.weights = weights;
					//						single_class_node.examples_ids = IntegerofArray_toVector(selected_examples);
					//						Globals.Node_Id++;
					//						single_class_node.class_label = selected_class;
					//						Globals.Tree_Nodes.add(single_class_node);
					//						}
					//							 */
					//
					//						} else // missing numerical
					//						{
					//							Tree_Branchs less_than_branch = new Tree_Branchs();
					//							Tree_Branchs greater_branch = new Tree_Branchs();
					//							// less_than_branch.attribue_id=selected_att.getAtt_id();
					//							// greater_branch.attribue_id=selected_att.getAtt_id();
					//							less_than_branch.branch_value = Double.toString(selected_att.getSplitPoint());
					//							greater_branch.branch_value = Double.toString(selected_att.getSplitPoint());
					//							less_than_branch.branch_operator = "<=";
					//							greater_branch.branch_operator = ">";
					//
					//							Vector<Integer> less_than_ex = new Vector<Integer>();
					//							Vector<Integer> greater_ex = new Vector<Integer>();
					//							Vector<Double> less_than_weights = new Vector<Double>();
					//							Vector<Double> greater_weights = new Vector<Double>();
					//							/*	
					//						for (int i = 0; i < selected_examples.length; i++) { // retrieve the old row
					//							if (selected_att.getNum_values()[selected_examples[i]] <= selected_att.getSplitPoint()) {// add to the less/equal// examples
					//								less_than_ex.add(selected_examples[i]);
					//								less_than_weights.add(weights.get(i));
					//							}
					//							else {
					//								greater_ex.add(selected_examples[i]);
					//								greater_weights.add(weights.get(i));
					//							}
					//							// add to the greater examples
					//						}
					//							 */
					//							less_than_ex=selected_att.getNum_outcomes().get(0).IdS;
					//							less_than_weights=selected_att.getNum_outcomes().get(0).weights;
					//							greater_ex=selected_att.getNum_outcomes().get(1).IdS;
					//							greater_weights=selected_att.getNum_outcomes().get(1).weights;
					//
					//
					//							// Vector<Integer> selected_Ids =outcomes.get(i).IdS;
					//							// Vector<Double> selected_weights
					//							// =outcomes.get(i).weights;
					//
					//							if (selected_att.getCurrent_missing() != null) {
					//								Vector<Double> missing_weights_less = (Vector<Double>) missing_val.weights.clone();//
					//								Vector<Double> missing_weights_greater = (Vector<Double>) missing_val.weights.clone();//
					//								int total_number_cases_in_outcomes = selected_examples.length- missing_val_index.size();//
					//								Double less_than_factor_weight = less_than_ex.size()/ (double) total_number_cases_in_outcomes;//
					//								Double greater_factor_weight = greater_ex.size()/ (double) total_number_cases_in_outcomes;//
					//
					//								less_than_branch.branch_factor=less_than_factor_weight;
					//								greater_branch.branch_factor=greater_factor_weight;
					//
					//								less_than_ex.addAll(missing_val_index);//
					//								greater_ex.addAll(missing_val_index);
					//
					//								for (int j = 0; j < missing_weights_less.size(); j++)//
					//								{
					//									missing_weights_less.set(j,missing_val.weights.get(j)* less_than_factor_weight);
					//									missing_weights_greater.set(j,missing_val.weights.get(j)* greater_factor_weight);
					//
					//								}//
					//
					//								less_than_weights.addAll(missing_weights_less);//
					//								greater_weights.addAll(missing_weights_greater);
					//							}
					//
					//							Integer[] less_than_ids = Integer_Vector_toArray(less_than_ex);
					//							Integer[] greater_ids = Integer_Vector_toArray(greater_ex);
					//
					//							boolean do_not_split=false;
					//
					//							if (Main.calc_sum(less_than_weights)<Globals.min_inst_4leaf || Main.calc_sum(greater_weights)<Globals.min_inst_4leaf)	
					//							{ do_not_split=true;}
					//
					//							if (do_not_split==false)
					//							{
					//								Node decision_node = new Node("Decision", Globals.Node_Id);
					//								decision_node.attribute = selected_att;
					//								decision_node.weights = weights;
					//
					//								Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
					//								decision_node.class_label= result.getMost_Frequent_class(); 
					//								Double E= result.getError();
					//								Double N=Main.calc_sum(weights);
					//								decision_node.N=N;
					//								decision_node.E=E;
					//								decision_node.examples_ids=IntegerofArray_toVector(selected_examples);
					//								//decision_node.Node_Error=E+Main.compute_error(E, N);
					//
					//								decision_node.parent_id = p_id;
					//								if (p_id != 0) {
					//									causing_branch.children_id = Globals.Node_Id;
					//								}
					//								Globals.Tree_Nodes.add(decision_node);
					//								Globals.Node_Id++;
					//
					//								Globals.Tree_Nodes.get(decision_node.id - 1).tree_branchs .add(less_than_branch);
					//								Globals.Tree_Nodes.get(decision_node.id - 1).tree_branchs.add(greater_branch);
					//
					//								composeSubTree(less_than_ids, decision_node.id,	less_than_branch, less_than_weights);
					//								composeSubTree(greater_ids, decision_node.id,greater_branch, greater_weights);
					//							}
					//							else  //do_not_split=true, then it is a leaf
					//							{Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
					//							selected_class= result.getMost_Frequent_class(); 
					//							Double E= result.getError();
					//							Double N=Main.calc_sum(weights);
					//							//selected_class = max_class(classes);
					//
					//							Node single_class_node = new Node("Leaf", Globals.Node_Id);
					//							single_class_node.N=N;
					//							single_class_node.E=E;
					//							//single_class_node.Node_Error=E+Main.compute_error(E, N);
					//
					//
					//							if (p_id != 0) {
					//								causing_branch.children_id = Globals.Node_Id;
					//							}
					//							single_class_node.parent_id = p_id;
					//							single_class_node.weights = weights;
					//							single_class_node.examples_ids = IntegerofArray_toVector(selected_examples);
					//							Globals.Node_Id++;
					//							single_class_node.class_label = selected_class;
					//							Globals.Tree_Nodes.add(single_class_node);
					//							}
					//
					//
					//
					//						}
					//					}

					// else no missing attribute
					//					else {
					if (selected_att.getType() == "nominal") {
						// for each possible value vi of Ai
						Vector<Outcome> outcomes = (Vector<Outcome>) Get_V_Outcomes(selected_att.getOutcomes()); // outcomes in case of nominal
						Vector<Integer[]> selected_Ids_arr=new Vector<Integer[]>();
						Vector<Tree_Branchs> new_branch = new Vector<Tree_Branchs>();
						double Error_if_split=0;

						for (int i = 0; i < outcomes.size(); i++) 
						{
							// add new tree branch below root, corresponding to
							// the test A=vi
							Tree_Branchs new_branch1 = new Tree_Branchs();
							new_branch1.branch_value = outcomes.get(i).value;
							new_branch1.branch_factor=outcomes.get(i).IdS.size()/(double) selected_examples.length;
							new_branch1.branch_operator = "=";
							new_branch.add(new_branch1);



							Vector<Integer> selected_Ids = outcomes.get(i).IdS;

							selected_Ids_arr.add(Integer_Vector_toArray(selected_Ids));
						}

						/*
						boolean do_not_split=false;
						boolean cond=true;
						for (int i = 0; i < outcomes.size(); i++)
							{
									if (Main.calc_sum(outcomes.get(i).weights)<Globals.min_inst_4leaf)	
											{cond= cond && true;}
									else 
											{cond= cond && false;}
									if (cond==true)
											{do_not_split=true;} //leaf 

							}*/


						/*
						if (do_not_split==false)//decision
							{
						 */
						Node decision_node = new Node("Decision", Globals.Node_Id);
						decision_node.attribute = selected_att;

						decision_node.Node_outcomes=(Vector<Outcome>) Get_V_Outcomes(selected_att.getOutcomes());
						decision_node.weights = weights;

						Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
						decision_node.class_label= result.getMost_Frequent_class(); 
						Double E= result.getError();
						Double N=PWC45.calc_sum(weights);
						//							if (E==0.5*N) //equal cases of classes for 2 classes problem
						//							{
						//								if (p_id==0)//first node
						//								{
						//									//select the first class in the file name
						//									String[] p_classes_names=Globals.classes_names; 
						//									String p_selected_class="p1\u2192"+p_classes_names[0]+", p2\u2192"+p_classes_names[1].substring(0,p_classes_names[1].length()-1);
						//									decision_node.class_label=p_selected_class;
						//								}
						//								else 
						//								{
						//									//select the class of parent node
						//									
						//									decision_node.class_label= Globals.Tree_Nodes.get(p_id-1).class_label;
						//								}
						//									
						//							}
						decision_node.N=N;
						decision_node.E=E;
						decision_node.examples_ids=IntegerofArray_toVector(selected_examples);
						//decision_node.Node_Error=E+Main.compute_error(E, N);
						decision_node.parent_id = p_id;
						if (p_id != 0) {
							causing_branch.children_id = Globals.Node_Id;
						}
						Globals.Tree_Nodes.add(decision_node);
						Globals.Node_Id++;

						for (int j = 0; j < outcomes.size(); j++) {
							Globals.Tree_Nodes.get(decision_node.id - 1).tree_branchs.add(new_branch.get(j));
							Vector<Double> sub_outcome_weights=Globals.Tree_Nodes.get(decision_node.id - 1).Node_outcomes.get(j).weights;
							Integer[] sub_outcome_Ids =selected_Ids_arr.get(j).clone(); 
							composeSubTree(sub_outcome_Ids, decision_node.id, new_branch.get(j), sub_outcome_weights  );
							if (Globals.Tree_Nodes.get(new_branch.get(j).children_id-1).type.equals("Leaf"))
							{Error_if_split+=Globals.Tree_Nodes.get(new_branch.get(j).children_id-1).E;
							}
							else
							{Error_if_split+=Globals.Tree_Nodes.get(new_branch.get(j).children_id-1).Node_Children_Error;  //for parent node
							}
						}

						if (Error_if_split>= E-Globals.Epsilon) //see whether we would have been no worse off with a leaf
						{ // collapse tree to a leaf
							//System.out.println("Collapse the tree to a leaf node at Node id"+Globals.Node_Id);
							decision_node.type="Leaf";
							decision_node.Node_Children_Error=(double) 0;
							for (int k=decision_node.tree_branchs.size()-1;k>=0;k--)
							{
								Globals.Tree_Nodes.remove(new_branch.get(k).children_id-1);
								Globals.Node_Id--;
								decision_node.tree_branchs.remove(k);
								//		    	decision_node.examples_ids.addAll(outcomes.get(k).IdS);
							}

						}
						else
						{//decided to keep the split, then update the error E to be the error from splitting
							Globals.Tree_Nodes.get(decision_node.id - 1).Node_Children_Error=Error_if_split;
						}

					}

					/*
						else  //do_not_split=true, then it is a leaf
						{
								Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
								selected_class= result.getMost_Frequent_class(); 
							    Double E= result.getError();
							    Double N=Main.calc_sum(weights);
								//selected_class = max_class(classes);

								Node single_class_node = new Node("Leaf", Globals.Node_Id);
								single_class_node.N=N;
								single_class_node.E=E;
								single_class_node.Node_Error=E+Main.compute_error(E, N);


								if (p_id != 0) {
									causing_branch.children_id = Globals.Node_Id;
							}

							single_class_node.parent_id = p_id;
							single_class_node.weights = weights;
							single_class_node.examples_ids = IntegerofArray_toVector(selected_examples);
							Globals.Node_Id++;
							single_class_node.class_label = selected_class;
							Globals.Tree_Nodes.add(single_class_node);
							}
							}
					 */





					/*						else // no missing and numerical

						{

							Tree_Branchs less_than_branch = new Tree_Branchs();
							Tree_Branchs greater_branch = new Tree_Branchs();

							less_than_branch.branch_value = Double.toString(selected_att.getSplitPoint());
							greater_branch.branch_value = Double.toString(selected_att.getSplitPoint());
							less_than_branch.branch_operator = "<=";
							greater_branch.branch_operator = ">";

							Vector<Integer> less_than_ex = new Vector<Integer>();
							Vector<Integer> greater_ex = new Vector<Integer>();
							Vector<Double> less_than_weights = new Vector<Double>();
							Vector<Double> greater_weights = new Vector<Double>();

							for (int i = 0; i < selected_examples.length; i++) { // retrieve// the// old				// row
								if (selected_att.getNum_values()[selected_examples[i]] <= selected_att.getSplitPoint())
								{// add to the less/equal
									// examples
									less_than_ex.add(selected_examples[i]);
									less_than_weights.add(weights.get(i));
								} else {
									greater_ex.add(selected_examples[i]);
									greater_weights.add(weights.get(i));
								}
								// add to the greater examples
							}



							Integer[] less_than_ids = Integer_Vector_toArray((Vector<Integer>)less_than_ex.clone());
							Integer[] greater_ids = Integer_Vector_toArray((Vector<Integer>) greater_ex.clone());
							less_than_branch.branch_factor=less_than_ids.length/(double) (less_than_ids.length + greater_ids.length);
							greater_branch.branch_factor=greater_ids.length/(double) (less_than_ids.length + greater_ids.length);

						boolean do_not_split=false;

						if (Main.calc_sum(less_than_weights)<Globals.min_inst_4leaf||Main.calc_sum(greater_weights)<Globals.min_inst_4leaf)	
							{ do_not_split=true;}

					if (do_not_split==false)
					{
							Node decision_node = new Node("Decision", Globals.Node_Id);
							decision_node.attribute = selected_att;
							decision_node.Node_outcomes=(Vector<Outcome>) Get_V_num_Outcomes(selected_att.getNum_outcomes());
							decision_node.weights = weights;

							Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
							decision_node.class_label= result.getMost_Frequent_class(); 
							Double E= result.getError();
							Double N=Main.calc_sum(weights);
							decision_node.N=N;
							decision_node.E=E;
							decision_node.examples_ids=IntegerofArray_toVector(selected_examples);
							//decision_node.Node_Error=E+Main.compute_error(E, N);
							decision_node.parent_id = p_id;

							if (p_id != 0) {
								causing_branch.children_id = Globals.Node_Id;
							}
							Globals.Tree_Nodes.add(decision_node);
							Globals.Node_Id++;
							double Error_if_split=0;

							Globals.Tree_Nodes.get(decision_node.id - 1).tree_branchs .add(less_than_branch);
							composeSubTree(less_than_ids, decision_node.id,	less_than_branch, less_than_weights);
							if (Globals.Tree_Nodes.get(less_than_branch.children_id-1).type.equals("Leaf"))
							{Error_if_split+=Globals.Tree_Nodes.get(less_than_branch.children_id-1).E;
							}
							else
							{Error_if_split+=Globals.Tree_Nodes.get(less_than_branch.children_id-1).Node_Children_Error;  //for parent node
							}
							Globals.Tree_Nodes.get(decision_node.id - 1).tree_branchs.add(greater_branch);

							composeSubTree(greater_ids, decision_node.id,greater_branch, greater_weights);
							if (Globals.Tree_Nodes.get(greater_branch.children_id-1).type.equals("Leaf"))
							{Error_if_split+=Globals.Tree_Nodes.get(greater_branch.children_id-1).E;
							}
							else
							{Error_if_split+=Globals.Tree_Nodes.get(greater_branch.children_id-1).Node_Children_Error;  //for parent node
							}

							if (Error_if_split>= E-Globals.Epsilon) //see whether we would have been no worse off with a leaf
							{ // collapse tree to a leaf
								System.out.println("Collapse the tree to a leaf node at Node id"+Globals.Node_Id);
								decision_node.type="Leaf";
								decision_node.Node_Children_Error=(double) 0;
								for (int k=decision_node.tree_branchs.size()-1;k>=0;k--)
								{
									Globals.Tree_Nodes.remove(decision_node.tree_branchs.get(k).children_id-1);
									Globals.Node_Id--;
									decision_node.tree_branchs.remove(k);
									//decision_node.examples_ids.addAll(decision_node.Node_outcomes.get(k).IdS);
								}

							}
							else
							{//decided to keep the split, then update the error E to be the error from splitting
								Globals.Tree_Nodes.get(decision_node.id - 1).Node_Children_Error=Error_if_split;
							}


					 * }

					else  //do_not_split=true, then it is a leaf
					{Multiple_Variables result = Multiple_Variables.max_class(classes, weights);
					selected_class= result.getMost_Frequent_class(); 
				    Double E= result.getError();
				    Double N=Main.calc_sum(weights);
					//selected_class = max_class(classes);

					Node single_class_node = new Node("Leaf", Globals.Node_Id);
					single_class_node.N=N;
					single_class_node.E=E;
					single_class_node.Node_Error=E+Main.compute_error(E, N);


					if (p_id != 0) {
						causing_branch.children_id = Globals.Node_Id;
					}
					single_class_node.parent_id = p_id;
					single_class_node.weights = weights;
					single_class_node.examples_ids = IntegerofArray_toVector(selected_examples);
					Globals.Node_Id++;
					single_class_node.class_label = selected_class;
					Globals.Tree_Nodes.add(single_class_node);
					}


						}*/

					//}
					//}


				}
			}
		}



		return null;
	}



	@SuppressWarnings("unchecked")
	private static Vector<Outcome> Get_V_Outcomes(Vector<Outcome> outcomes) {
		// TODO Auto-generated method stub
		Vector<Outcome> Cloned_V_Outcomes=new Vector<Outcome>();
		for (int i=0;i<outcomes.size();i++)
		{
			Outcome new_outcome=new Outcome ();
			new_outcome.IdS=(Vector<Integer>) outcomes.get(i).IdS.clone();
			new_outcome.classes=(Vector<String>) outcomes.get(i).classes.clone();
			new_outcome.info_Ti=outcomes.get(i).info_Ti;
			new_outcome.Ti_by_T=outcomes.get(i).Ti_by_T;
			new_outcome.value=outcomes.get(i).value;
			new_outcome.weights=(Vector<Double>) outcomes.get(i).weights.clone();
			Cloned_V_Outcomes.add(new_outcome);
		}

		return Cloned_V_Outcomes;
	}

	private static float worth(double ThisInfo, double ThisGain, double MinGain) {
		if ((ThisGain>= MinGain-Globals.Epsilon) && (ThisInfo>Globals.Epsilon))
		{return (float) (ThisGain/ThisInfo);}
		else {return (float) -Globals.Epsilon;}
	}

	static double[] Double_Vector_toArray(Vector<Double> V) {
		double[] Arr = new double[V.size()];
		for (int i = 0; i < V.size(); i++) {
			Arr[i] = V.get(i);
		}
		return Arr;
	}
	static Integer[] Integer_Vector_toArray(Vector<Integer> V) {
		Integer[] Arr = new Integer[V.size()];
		for (int i = 0; i < V.size(); i++) {
			Arr[i] = V.get(i);
		}
		return Arr;
	}

	private static Vector<Integer> IntegerofArray_toVector(Integer[] L) {
		Vector<Integer> V = new Vector<Integer>();
		for (int i = 0; i < L.length; i++) {
			V.add(L[i]);
		}
		return V;
	}

	static String[] String_Vector_toArray(Vector<String> V) {
		String[] Arr = new String[V.size()];
		for (int i = 0; i < V.size(); i++) {
			Arr[i] = V.get(i);
		}
		return Arr;
	}

	static Vector<String> getClasses(Integer[] selected_examples) {

		Vector<String> classes = new Vector<String>();

		for (int i = 0; i < selected_examples.length; i++) {
			classes.add(Globals.classes.get(selected_examples[i]));
		}

		return classes;
	}

	private static Vector<Vector<String>> get_all_Attribute_value(int attribute_id) {

		Vector<Vector<String>> values = new Vector<Vector<String>>();
		Vector<String> one_pair_values=new Vector<String>();
		// Vector<Double> att_weights = new Vector<Double>();

		for (int i = 0; i < Globals.Training_data.size(); i++) {
			Vector<String> temp = Globals.Training_data.get(i).get(attribute_id);
			boolean p1_is_missing=false;
			boolean p2_is_missing=false;
			if (temp.get(0).equals(" ") | temp.get(0).equals("?") | temp.get(0).equals("")) {
				one_pair_values.add("?");
				p1_is_missing=true;
			}
			else {one_pair_values.add(temp.get(0));}
			if (temp.get(1).equals(" ") | temp.get(1).equals("?") | temp.get(1).equals(""))
			{
				one_pair_values.add("?");
				p2_is_missing=true;
			}
			else {one_pair_values.add(temp.get(1));}


			if (p1_is_missing |p2_is_missing)
			{
				System.out.println("This version of the code cannot handle missing values");
				System.exit(0);
			}
			else {
				values.add(temp);
				// Globals.attribues.get(attribute_id).weights.add((double) 1);
			}

		}
		// Globals.attribues.get(attribute_id).setWeights(att_weights);
		return values;
	}



	public static float log2 (float num) {
		float result;

		if (num<=0)
		{result= 0;}
		else
		{
			result=	 (float) (Math.log((float)num) /(float) Globals.log2);
		}
		return result;
	}

	// double Entropy_2 (int x, int y)
	// {
	// double proportion_x = x/(double)(x+y);
	// double proportion_y = y/(double)(x+y);
	// return
	// (-proportion_x*log2(proportion_x))-(proportion_y*log2(proportion_y));
	//
	// }

	public static double Entropy(Double[] ListOfFreq) {
		float sum = 0;
		for (Double i : ListOfFreq) {
			sum += i;
		}
		Float[] proportion;
		proportion = new Float[ListOfFreq.length];

		double Ent = 0;
		for (int i = 0; i < ListOfFreq.length; i++) {
			if (ListOfFreq[i] != 0.0) {
				proportion[i] = (float) (ListOfFreq[i] /  sum);
				Ent += (-proportion[i] * log2(proportion[i]));
			}
		}

		return Ent;
	}


	// public static Double[] freqCi(Vector<String> classes,Integer[] selected_ids, Vector<Double> weights) {

	public static Pair<Vector<String>, Vector<Double>> freqCi(Vector<String> classes,Integer[] selected_ids, Vector<Double> weights) {

		Vector<String> disticive_classes = new Vector<String>();
		Vector<Double> disticive_classes_freq = new Vector<Double>();
		// initially add the class of first row, then loop to add the rest
		disticive_classes.add(classes.get(0));
		disticive_classes_freq.add(weights.get(0));
		for (int i = 1; i < classes.size(); i++) {
			boolean exist = true;
			String Ci = classes.get(i);
			for (int j = 0; j < disticive_classes.size(); j++) {
				String comparingItem = disticive_classes.get(j);
				if (Ci.equalsIgnoreCase(comparingItem)) {
					exist = true;
					double temp = disticive_classes_freq.get(j)	+ weights.get(i);
					disticive_classes_freq.set(j, temp);
					break;
				} else {
					exist = false;
				}

			}
			if (!exist) {
				disticive_classes.add(Ci);
				disticive_classes_freq.add(weights.get(i));
			}
		}
		//Pair<Vector<String>, Vector<Double>> results=new Pair<Vector<String>, Vector<Double>>();
		//results.setFirst(disticive_classes);
		//results.setSecond(disticive_classes_freq);
		//return results ;

		return new Pair<Vector<String>, Vector<Double>> (disticive_classes,disticive_classes_freq);
		//return disticive_classes_freq.toArray(new Double[disticive_classes_freq.size()]);
	}

	/*public static String max_class(Vector<String> classes) {
		Vector<String> disticive_classes = new Vector<String>();
		Vector<Integer> disticive_classes_freq = new Vector<Integer>();
		// initally add the class of first row, then loop to add the rest
		disticive_classes.add(classes.get(0));
		disticive_classes_freq.add(1);
		for (int i = 1; i < classes.size(); i++) {
			boolean exist = true;
			String Ci = classes.get(i);
			for (int j = 0; j < disticive_classes.size(); j++) {
				String comparingItem = disticive_classes.get(j);
				if (Ci.equalsIgnoreCase(comparingItem)) {
					exist = true;
					int temp = disticive_classes_freq.get(j) + 1;
					disticive_classes_freq.set(j, temp);
					break;
				} else {
					exist = false;
				}

			}
			if (!exist) {
				disticive_classes.add(Ci);
				disticive_classes_freq.add(1);
			}
		}

		int maxValue = disticive_classes_freq.get(0);
		String max_class = disticive_classes.get(0);
		for (int i = 1; i < disticive_classes_freq.size(); i++) {
			if (disticive_classes_freq.get(i) > maxValue) {
				maxValue = disticive_classes_freq.get(i);
				max_class = disticive_classes.get(i);
			}
		}
		return max_class;
	}
	 */

	public static double[] convert_Str2double(String[] mylist, int att_id) {
		double[] out;
		out = new double[mylist.length];

		for (int i = 0; i < mylist.length; i++) {

			try {
				out[i] = Double.parseDouble(mylist[i]);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				System.out.println("Attribute number "+Integer.toString(att_id+1)+"contains one or more non numerical value(s)");
			}

		}


		return out;
	}

	public static String find_leaf(Vector<Vector<String>> paired_instance,
			int node_id) {
		// String
		// selected_att=Globals.Tree_Nodes.get(node_id).attribute.getName();
		Node n= new Node();
		if (Globals.Prunning)
		{n = Globals.Pruned_Tree_Nodes.get(node_id - 1);}
		else
		{n = Globals.Tree_Nodes.get(node_id - 1);}

		String out = "";
		Vector<String> instance= new Vector<String>();

		for (int i = 0; i < Globals.attribues.size(); i++)
		{
			String p1= paired_instance.get(i).get(0);
			String p2= paired_instance.get(i).get(1);

			Double[] V_i = {Double.parseDouble(p1),Double.parseDouble(p2)};

			if (Collections.min(Arrays.asList(V_i))==Collections.max(Arrays.asList(V_i)))//equal
				//	if (Math.min(Double.parseDouble(p1),Double.parseDouble(p2))==Math.max(Double.parseDouble(p1),Double.parseDouble(p2)))
			{instance.add("eq");}
			else if (Double.parseDouble(p1)==Collections.min(Arrays.asList(V_i))) 
				//	else if (Double.parseDouble(p1)==Math.min(Double.parseDouble(p1),Double.parseDouble(p2)))
			{instance.add("min");}
			else//max
			{instance.add("max");}
		}

		if (n.type == "Decision") {
			String selected_att = n.attribute.getName();
			// find header_id
			int matching_att_id = 0;
			for (int i = 0; i < Globals.attribues.size(); i++) {
				if (selected_att.equals(Globals.attribues.get(i).getName())) {
					matching_att_id = i;
					break;
				}
			}
			for (int j = 0; j < n.tree_branchs.size(); j++) {
				boolean cond_result = false;

				//if (n.tree_branchs.get(j).branch_operator.equals("=")) {
				if (instance.get(matching_att_id).equalsIgnoreCase(n.tree_branchs.get(j).branch_value)) {
					cond_result = true;
				}
				//				} else if (n.tree_branchs.get(j).branch_operator.equals("<=")) {
				//					if (Double.parseDouble(instance.get(matching_att_id)) <= Double.parseDouble(n.tree_branchs.get(j).branch_value)) {
				//						cond_result = true;
				//					}
				//					;
				//				} else if (n.tree_branchs.get(j).branch_operator.equals(">")) {
				//					if (Double.parseDouble(instance.get(matching_att_id)) > Double.parseDouble(n.tree_branchs.get(j).branch_value)) {
				//						cond_result = true;
				//					}
				//				}

				if (cond_result) {
					out = out+ find_leaf(paired_instance, n.tree_branchs.get(j).children_id);
				}

			}
		} else {
			out = n.class_label;

		}
		return out;

	}

	public static void testing_using_trainingdata(String output_path) {
		Vector<String> actual_class = new Vector<String>();
		Vector<String> predicted_class = new Vector<String>();

		for (int i = 0; i < Globals.Training_data.size(); i++) {
			Vector<Vector<String>> instance = Globals.Training_data.get(i);
			actual_class.add(Globals.classes.get(i));
			String class_output = find_leaf(instance, 1);
			predicted_class.add(class_output);
		}
		FileWriter fstream;
		try {
			fstream = new FileWriter(output_path+"test_output.csv");
			double accuracy = 0;
			BufferedWriter f_out = new BufferedWriter(fstream);
			String line_sep = System.getProperty("line.separator"); // newline
			f_out.write("Atcual,Predicted" + line_sep);
			for (int i = 0; i < actual_class.size(); i++) {
				f_out.write(actual_class.get(i) + "," + predicted_class.get(i)
						+ line_sep);
				if (actual_class.get(i).equals(predicted_class.get(i))) {
					accuracy++;
				}
			}
			accuracy = accuracy / (double) actual_class.size() * 100;
			//System.out.println("Accuracy= " + accuracy + "%");
			System.out.print(accuracy + "%");
			f_out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return;
	}

	static double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	static double roundOneDecimals(double d) {
		DecimalFormat OneDForm = new DecimalFormat("#.#");
		return Double.valueOf(OneDForm.format(d));
	}


	public static Vector<Double> ArrayofDouble_toVector(
			double[] sorted_num_values) {
		Vector<Double> V = new Vector<Double>();
		for (int i = 0; i < sorted_num_values.length; i++) {
			V.add(sorted_num_values[i]);
		}
		return V;

	}

	public static String write_decision_rules_v2(int index, int level) {
		String out = "";

		//int space_length = 5;

		Node n = Globals.Tree_Nodes.get(index - 1);

		if (n.type == "Decision") {
			out = out + "\r\n";
			//space_length = 5 * level;
			String markers = "";
			for (int m = 0; m < level; m++) {
				markers += "|";
				for (int k = 0; k < 5; k++) {
					markers += " ";
				}

			}
			level++;

			for (int j = 0; j < n.tree_branchs.size(); j++) {

				String Cond = "R(" + n.attribute.getName() +"(p1),{" +n.attribute.getName()+"(p1),"+n.attribute.getName()+"(p2)})"+ " "	+ n.tree_branchs.get(j).branch_operator + " "	+ n.tree_branchs.get(j).branch_value ;
				if (j == 0) {
					//out = out/* +String.format("%" + space_length + "s", "") */	+ markers + "If " + Cond + " then ";
					out = out/* +String.format("%" + space_length + "s", "") */	+ markers +  Cond +" : ";

				} else {
					//out = out/* +String.format("%" + space_length + "s", "") */	+ markers + "Else If " + Cond + " then ";
					out = out/* +String.format("%" + space_length + "s", "") */	+ markers + Cond+" : " ;
				}

				out = out+ write_decision_rules_v2(	n.tree_branchs.get(j).children_id, level);

			}
		} else // leaf
		{

			out = n.class_label;
			if (n.E==0)
			{
				out = out + " (" + roundTwoDecimals(n.N) + ")";
			}
			else
			{out = out + " (" + roundTwoDecimals(n.N) + "/"+ roundTwoDecimals(n.E) + ")";}
			out = out + "\r\n";
			level--;

		}

		return out;
	}

	public static double calc_sum(Vector<Double> Weights) {
		double sum_weights = 0;

		for (int i = 0; i < Weights.size(); i++) {
			sum_weights += Weights.get(i);
		}
		return sum_weights;
	}

	public static void Split_for_testing()
	{
		Vector<String> disticive_classes = new Vector<String>();
		Vector<Double> disticive_classes_freq = new Vector<Double>();
		Vector<Vector<Integer>> indexes_in_classes= new Vector<Vector<Integer>>();  

		// initially add the class of first row, then loop to add the rest
		disticive_classes.add(Globals.all_classes.get(0));
		disticive_classes_freq.add(1.0);
		Vector<Integer> first_index_vector=new Vector<Integer>();
		first_index_vector.add(0);
		indexes_in_classes.add( first_index_vector);

		for (int i = 1; i < Globals.all_classes.size(); i++) {
			boolean exist = true;
			String Ci = Globals.all_classes.get(i);
			for (int j = 0; j < disticive_classes.size(); j++) {
				String comparingItem = disticive_classes.get(j);
				if (Ci.equalsIgnoreCase(comparingItem)) {
					exist = true;
					double temp = disticive_classes_freq.get(j)	+ 1;
					disticive_classes_freq.set(j, temp);
					indexes_in_classes.get(j).add(i);
					break;
				} 
				else {
					exist = false;
				}
			}
			if (!exist) {
				disticive_classes.add(Ci);
				disticive_classes_freq.add(1.0);
				Vector<Integer> new_index_vector=new Vector<Integer>();
				new_index_vector.add(i);
				indexes_in_classes.add(new_index_vector);
			}
		}
		//disticive_classes contains the different classes
		//disticive_classes_freq contains the different classes frequencies
		//all_classes size is the total frequencies
		//calculate the relative frequency
		//		Vector<Double> disticive_classes_relative_freq = new Vector<Double>();
		/*for (int i=0; i<disticive_classes_freq.size() ; i++) 
		{disticive_classes_relative_freq.add(disticive_classes_freq.get(i)/(double)Globals.all_classes.size());
		}
		 */
		for (int i=0; i<disticive_classes_freq.size() ; i++) 
		{
			loop: while (true)  //there is a break inside the loop if there is no more elements to split
			{

				Random randomGenerator = new Random();

				// I am going to generate three random numbers from this class , send two to training and one for testing

				int first_random = randomGenerator.nextInt(indexes_in_classes.get(i).size()/2);
				Vector<Vector<String>> first_pair=new Vector<Vector<String>>();
				first_pair.add(Globals.All_data.get(indexes_in_classes.get(i).get(first_random*2)));
				first_pair.add(Globals.All_data.get(indexes_in_classes.get(i).get(first_random*2+1)));
				Globals.Training_data.add(first_pair);
				String first_pair_C1=Globals.all_classes.get(indexes_in_classes.get(i).get(first_random*2));
				String first_pair_C2=Globals.all_classes.get(indexes_in_classes.get(i).get(first_random*2+1));

				Globals.classes.add("p1\u2192"+first_pair_C1+", p2\u2192"+first_pair_C2);

				//Globals.classes.add("p\u2081"+" \u2192 "+first_pair_C1+", "+"p\u2082"+" \u2192 "+first_pair_C2);
				indexes_in_classes.get(i).remove(first_random);
				if (indexes_in_classes.get(i).size()==0) 
				{
					break loop;
				}

				int second_random = randomGenerator.nextInt(indexes_in_classes.get(i).size()/2);
				Vector<Vector<String>> second_pair=new Vector<Vector<String>>();
				first_pair.add(Globals.All_data.get(indexes_in_classes.get(i).get(second_random*2)));
				first_pair.add(Globals.All_data.get(indexes_in_classes.get(i).get(second_random*2+1)));
				Globals.Testing_data_prunning.add(second_pair);
				String second_pair_C1=Globals.all_classes.get(indexes_in_classes.get(i).get(second_random*2));
				String second_pair_C2=Globals.all_classes.get(indexes_in_classes.get(i).get(second_random*2+1));

				Globals.Testing_classes_prunning.add("p1\u2192"+second_pair_C1+", p2\u2192"+second_pair_C2);

				//Globals.Testing_classes_prunning.add("p\u2081"+" \u2192 "+second_pair_C1+", "+"p\u2082"+" \u2192 "+second_pair_C2);
				indexes_in_classes.get(i).remove(second_random);
				if (indexes_in_classes.get(i).size()==0) 
				{
					break loop;
				}


				int third_random = randomGenerator.nextInt(indexes_in_classes.get(i).size()/2);
				Vector<Vector<String>> third_pair=new Vector<Vector<String>>();
				third_pair.add(Globals.All_data.get(indexes_in_classes.get(i).get(third_random*2)));
				third_pair.add(Globals.All_data.get(indexes_in_classes.get(i).get(third_random*2+1)));
				Globals.Training_data.add(third_pair);
				String third_pair_C1=Globals.all_classes.get(indexes_in_classes.get(i).get(third_random*2));
				String third_pair_C2=Globals.all_classes.get(indexes_in_classes.get(i).get(third_random*2+1));

				Globals.classes.add("p1\u2192"+third_pair_C1+", p2\u2192"+third_pair_C2);
				//	Globals.classes.add("p\u2081"+" \u2192 "+third_pair_C1+", "+"p\u2082"+" \u2192 "+third_pair_C2);
				indexes_in_classes.get(i).remove(third_random);
				if (indexes_in_classes.get(i).size()==0) 
				{
					break loop;
				}


			} //end loop

		}//end for




	}

	public static double compute_error (double e, double N)
	{
		/*************************************************************************/
		/*									 */
		/*  Compute the additional errors if the error rate increases to the	 */
		/*  upper limit of the confidence level.  The coefficient is the	 */
		/*  square of the number of standard deviations corresponding to the	 */
		/*  selected confidence level.  (Taken from Documenta Geigy Scientific	 */
		/*  Tables (Sixth Edition), p185 (with modifications).)			 */
		/*									 */
		/*************************************************************************/
		double  [] Val = {  0,  0.001, 0.005, 0.01, 0.05, 0.10, 0.20, 0.40, 1.00};
		double  [] Dev = {4.0,  3.09,  2.58,  2.33, 1.65, 1.28, 0.84, 0.25, 0.00};

		double Coeff=0;

		double Val0, Pr;

		if ( ! convertIntToBoolean(Coeff) )
		{
			/*  Compute and retain the coefficient value, interpolating from the values in Val and Dev  */

			int i = 0;
			while ( Globals.CF > Val[i] )
			{
				i++;
			}
			Coeff = Dev[i-1] + (Dev[i] - Dev[i-1]) * (Globals.CF - Val[i-1]) /(Val[i] - Val[i-1]);
			Coeff = Coeff * Coeff;

		}
		if ( e < 1E-6 )
		{
			return N * (1 - Math.exp(Math.log(Globals.CF) / N));
		}
		else  if ( e < 0.9999 )
		{
			Val0 = N * (1 - Math.exp(Math.log(Globals.CF) / N));

			return Val0 + e * (compute_error( 1.0,N) - Val0);
		}

		else if ( e + 0.5 >= N )
		{
			return 0.67 * (N - e);
		}
		else
		{
			Pr = (e + 0.5 + Coeff/2  + Math.sqrt(Coeff * ((e + 0.5) * (1 - (e + 0.5)/N) + Coeff/4)) )  / (N + Coeff);
			return (N * Pr - e);
		}

	}//end the function Compute_error

	public static double EstimateErrors(int Node_id)
	{	
		double TreeErrors = 0;
		int max_br=-1;
		double max_br_factor=0;
		Node this_node=Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(Node_id));

		if (this_node.type.equals("Leaf"))
		{
			TreeErrors=this_node.E+PWC45.compute_error(this_node.E, this_node.N);
			this_node.Errors=TreeErrors;
			//this_node.E=this_node.Errors; //update E with the value of Tree errors as Original C4.5 do

			//System.out.println(this_node.class_label+"("+this_node.N+": "+this_node.E+"/ "+Globals.df2.format(this_node.Errors)+")");


			return TreeErrors; 
		}
		boolean max_br_is_leaf=true;

		for (int i=0;i<this_node.tree_branchs.size();i++)
		{
			TreeErrors+=EstimateErrors(this_node.tree_branchs.get(i).children_id);
			if (this_node.tree_branchs.get(i).branch_factor>=max_br_factor)
			{
				max_br=i;
				max_br_factor=this_node.tree_branchs.get(i).branch_factor;
			}

		}
		if (Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(this_node.tree_branchs.get(max_br).children_id)).type.equals("Decision"))
		{max_br_is_leaf=false;}


		double Branch_Errors;
		Vector<Node> alternative_Subtree =new Vector<Node>();
		if (max_br_is_leaf)
		{
			Branch_Errors=this_node.E+PWC45.compute_error(this_node.E, this_node.N);
		}

		else
		{
			MaxBr_Estimation Estimation_Result=MaxBr_Estimation.EstimateError_MaxBr(Node_id, max_br) ;
			Branch_Errors=Estimation_Result.getEstimated_Error();
			alternative_Subtree=Estimation_Result.getNew_subtree();
		}

		//System.out.println("Att_name:"+this_node.attribute.getName()+", val: "+Globals.df2.format((TreeErrors*100)/(double)(this_node.N+0.001))+"%, N: "+this_node.N+", TreeErrors:"+Globals.df2.format(TreeErrors)+", LeafErrors "+this_node.E+", Extra LeafErrors: "+Globals.df2.format(Main.compute_error(this_node.E, this_node.N)) + ", MaxBr" +max_br+ ", Branch Errors: "+Globals.df2.format(Branch_Errors));

		if ((this_node.E+PWC45.compute_error(this_node.E, this_node.N)<=Branch_Errors+0.1)&&
				(this_node.E+PWC45.compute_error(this_node.E, this_node.N)<=TreeErrors+0.1))
		{
			//	System.out.println("Replaced with leaf "+this_node.class_label);

			this_node.type="Leaf";
			this_node.Errors=this_node.E+PWC45.compute_error(this_node.E, this_node.N);

			//		int node_loc=Globals.Item_Location.get(this_node.id);

			Vector<Node> temp_subtree =Get_Subtree(this_node);
			Remove_Subtree(temp_subtree);
			//clear tree branches in this node
			//tree is updated
			for (int i=this_node.tree_branchs.size()-1;i>=0;i--)
			{this_node.tree_branchs.remove(i);

			}


		}
		else if(Branch_Errors<=TreeErrors+0.1)
		{
			int max_br_node_id=0;
			Double curr_err=0.0;
			//System.out.println("Replaced with branch: "+max_br);
			max_br_node_id=Replace_using_MaxBr(Node_id, max_br, alternative_Subtree);
			Node curr_node=Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(max_br_node_id));
			//subtree raising
			curr_node.Errors=EstimateErrors(max_br_node_id);
			curr_err=curr_node.Errors;
			return curr_err;

		}

		else
		{this_node.Errors=TreeErrors;
		}

		return this_node.Errors;

	}

	private static void Remove_Subtree(Vector<Node> temp_subtree) {
		// TODO Auto-generated method stub
		for (int i=temp_subtree.size()-1;i>=0;i--)
		{	
			Globals.Pruned_Tree_Nodes.removeElementAt(Globals.Item_Location.get(temp_subtree.get(i).id));
			Update_Item_Location(temp_subtree.get(i).id,Globals.Item_Location);
		}

	}

	private static Vector<Node> Get_Subtree(Node this_node) {
		// TODO Auto-generated method stub
		Vector<Node> subtree=new Vector<Node> ();
		for (int i=0;i<this_node.tree_branchs.size();i++)
		{ int child_id=this_node.tree_branchs.get(i).children_id;
		Node node=Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(child_id));
		subtree.add(node);
		if (node.type.equals("Decision"))
		{
			subtree.addAll(Get_Subtree(node));
		}
		}
		return subtree;
	}

	private static Vector<Node> Get_Subtree_inc_header(Node this_node) {
		// TODO Auto-generated method stub
		Vector<Node> subtree=new Vector<Node> ();
		subtree.add(Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(this_node.id)));
		subtree.addAll(Get_Subtree(this_node));
		return subtree;
	}

	public static void Tree_Prunning()
	{
		//System.out.println("Function Tree_Prunning starts here");

		//Globals.Pruned_Tree_Nodes= (Vector<Node>) Globals.Tree_Nodes.clone();
		Globals.Pruned_Tree_Nodes=Vector_Nodes_clone(Globals.Tree_Nodes);

		Globals.Item_Location= new Vector<Integer>();
		Globals.Item_Location.add((Integer) (-1));
		//item location variable is used as index mapping that contains the vector index of the nodes in the pruned tree vector
		for (int i=1; i<=Globals.Tree_Nodes.size();i++ )
		{
			Globals.Item_Location.add(i-1);
		}

		EstimateErrors( 1);

	} //end of function Tree_Prunning

	@SuppressWarnings("unchecked")
	private static Vector<Node> Vector_Nodes_clone(Vector<Node> Tree_Nodes) {
		// TODO Auto-generated method stub
		Vector<Node> returned_Nodes=new Vector<Node>();
		for (int i=0;i<Tree_Nodes.size();i++)
		{
			Node equivalent_node=Tree_Nodes.get(i);
			Node new_node=new Node(equivalent_node.type,equivalent_node.id);
			new_node.parent_id=equivalent_node.parent_id;
			new_node.attribute = equivalent_node.attribute;
			new_node.class_label = equivalent_node.class_label;
			new_node.N = equivalent_node.N;
			new_node.E = equivalent_node.E;
			new_node.Errors = equivalent_node.Errors;
			new_node.Node_Children_Error = equivalent_node.Node_Children_Error;
			new_node.all_children_are_leafs = equivalent_node.all_children_are_leafs;
			new_node.Node_outcomes = PWC45.Get_V_Outcomes(equivalent_node.Node_outcomes);
			new_node.examples_ids = (Vector<Integer>) equivalent_node.examples_ids.clone();
			new_node.weights = (Vector<Double>) equivalent_node.weights.clone();
			for (int j=0;j<equivalent_node.tree_branchs.size();j++)
			{ 
				Tree_Branchs old_branch=equivalent_node.tree_branchs.get(j);
				Tree_Branchs new_branch=new Tree_Branchs();

				new_branch.branch_operator=old_branch.branch_operator;
				new_branch.branch_value=old_branch.branch_value;
				new_branch.children_id=old_branch.children_id;
				new_branch.branch_factor=old_branch.branch_factor;

				new_node.tree_branchs.add(new_branch);
			}
			returned_Nodes.add(new_node);
		}
		return returned_Nodes;
	}





	public static void write_output_files_pruned(String output_path, String inputfilename) {
		FileWriter fstream;
		try {
			fstream = new FileWriter(output_path+inputfilename+"_pruned_out.csv");
			BufferedWriter f_out_pruned = new BufferedWriter(fstream);
			String line_sep = System.getProperty("line.separator"); // newline
			f_out_pruned.write("Id,parent,type,attribute,tree branch cond,tree branch value, child_node id,class");
			f_out_pruned.write(line_sep);
			for (int i = 0; i < Globals.Pruned_Tree_Nodes.size(); i++) {
				f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).id + " , ");
				f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).parent_id + " , ");
				f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).type + " , ");
				String attribute_val;
				try {
					attribute_val = Globals.Pruned_Tree_Nodes.get(i).attribute.getName();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					attribute_val = " ";
				}

				f_out_pruned.write(attribute_val + " , ");

				// f_out.write( line_sep);
				if (Globals.Pruned_Tree_Nodes.get(i).type == "Decision") {
					f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).tree_branchs.get(0).branch_operator + " , ");
					f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).tree_branchs.get(0).branch_value + " , ");
					f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).tree_branchs.get(0).children_id + ",");
					f_out_pruned.write(line_sep);
					for (int j = 1; j < Globals.Pruned_Tree_Nodes.get(i).tree_branchs.size(); j++) 
					{
						f_out_pruned.write(",,,,");
						f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).tree_branchs.get(j).branch_operator + " , ");
						f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).tree_branchs.get(j).branch_value + " , ");
						f_out_pruned.write(Globals.Pruned_Tree_Nodes.get(i).tree_branchs.get(j).children_id + ",");
						f_out_pruned.write(line_sep);
					}
				}
				else // leaf
				{
					f_out_pruned.write(",,," + Globals.Pruned_Tree_Nodes.get(i).class_label+ ",");
				}
				f_out_pruned.write(line_sep);
			}

			// Close the output stream
			f_out_pruned.close();



			// writing if-then decision file
			Writer dc_rules = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output_path+"Pruned_"+inputfilename	+ "_decision_rules.txt"), "UTF-8"));

			//	FileWriter fstream2 = new FileWriter(output_path+"Pruned_"+inputfilename	+ "_decision_rules.txt");
			//	BufferedWriter dc_rules = new BufferedWriter(fstream2);
			dc_rules.write("PWC4.5 Pruned Tree");
			dc_rules.write("\r\n");
			dc_rules.write("__________________");
			dc_rules.write("\r\n");

			dc_rules.write(write_decision_rules_pruned(1, 0));// pass the first node to start with , and level 0 for writing nested if statements

			int number_of_leafs = 0;
			for (int i = 0; i < Globals.Pruned_Tree_Nodes.size(); i++) {
				if (Globals.Pruned_Tree_Nodes.get(i).type == "Leaf") {
					number_of_leafs++;

				}

			}
			dc_rules.write("Number of Leaves:  " + number_of_leafs + line_sep);

			dc_rules.write("Size of the tree : " + Globals.Pruned_Tree_Nodes.size());

			// }
			dc_rules.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}//end function
	public static String write_decision_rules_pruned(int index, int level) {
		String out = "";
		//int space_length = 5;
		Node n = Globals.Pruned_Tree_Nodes.get(index - 1);

		if (n.type == "Decision") {
			out = out + "\r\n";
			//	space_length = 5 * level;
			String markers = "";
			for (int m = 0; m < level; m++) {
				markers += "|";
				for (int k = 0; k < 5; k++) {
					markers += " ";
				}
			}

			level++;

			for (int j = 0; j < n.tree_branchs.size(); j++) {
				String Cond = "R(" + n.attribute.getName() +"(p1),{" +n.attribute.getName()+"(p1),"+n.attribute.getName()+"(p2)})"+ " "	+ n.tree_branchs.get(j).branch_operator + " "	+ n.tree_branchs.get(j).branch_value ;
				//String Cond = "(" + n.attribute.getName() + " "	+ n.tree_branchs.get(j).branch_operator + "  "+ n.tree_branchs.get(j).branch_value + " )";
				if (j == 0) {
					//out = out + markers + "If " + Cond + " then ";
					out = out + markers +  Cond + " : ";

				} else {
					//out = out+ markers + "Else If " + Cond + " then ";
					out = out+ markers +  Cond + " : ";
				}

				out = out+ write_decision_rules_pruned(n.tree_branchs.get(j).children_id, level);
			}

		} else // leaf
		{
			//Integer[] selected_examples = Main.Integer_Vector_toArray(n.examples_ids);
			//Vector<String> classes = getClasses(selected_examples);

			// *test if all the instances belongs to the same class or majority class
			out = n.class_label;
			if (n.Errors==0.0)
			{
				out = out + " (" + roundOneDecimals(n.N) + ")";
			}
			else
			{
				out = out + " (" + roundOneDecimals(n.N) + "/"+ roundOneDecimals(n.Errors) + ")";
			}


			out = out + "\r\n";
			level--;

		}

		return out;
	}



	public static boolean convertIntToBoolean(double intValue)
	{
		return (intValue != 0);
	}

	//	private static Vector<Outcome> get_distinct_outcomes( String[] vals){
	//		Vector<Outcome> outs = new Vector<Outcome>();
	//		boolean exist = false;
	//		for(int index = 0; index < vals.length;index++){
	//			String item = vals[index];
	//
	//			for(int i = 0; i< outs.size();i++){
	//				String comparingItem = outs.get(i).value;
	//				if(item.equalsIgnoreCase(comparingItem)){
	//					exist = true;
	//					break;
	//				}else{
	//					exist = false;
	//				}
	//			}
	//			if(!exist){
	//				Outcome o = new Outcome();
	//				o.value = item;
	//				outs.add(o);
	//			}
	//		}
	//		return outs;
	//	}


	public static int Replace_using_MaxBr(int Node_Id, int max_br, Vector<Node> new_subtree)
	{
		Node this_node;

		if (Node_Id==1)
		{
			this_node=Globals.Pruned_Tree_Nodes.get(0);

			int p_id=this_node.parent_id;
			//int p_id_causing_branch=0;

			//			for (int i=0;i<Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(p_id)).tree_branchs.size();i++)
			//			{
			//				if (this_node.id==Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(p_id)).tree_branchs.get(i).children_id)
			//				{
			//					p_id_causing_branch=i;
			//					break;
			//				}
			//			}
			Vector<Node> temp_subtree=Get_Subtree_inc_header(this_node);
			PWC45.Remove_Subtree(temp_subtree);
			int last_item=Globals.Item_Location.size()-1;

			//for the first node
			new_subtree.get(0).id=1+last_item;
			//Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(p_id)).tree_branchs.get(p_id_causing_branch).children_id=new_subtree.get(0).id;

			new_subtree.get(0).parent_id=p_id;
			for (int j=0;j<new_subtree.get(0).tree_branchs.size();j++)
			{new_subtree.get(0).tree_branchs.get(j).children_id=new_subtree.get(0).tree_branchs.get(j).children_id+last_item;
			}

			//for all other nodes
			for (int i=1;i<new_subtree.size();i++)
			{   new_subtree.get(i).id=new_subtree.get(i).id+last_item;
			new_subtree.get(i).parent_id=new_subtree.get(i).parent_id+last_item;
			for (int j=0;j<new_subtree.get(i).tree_branchs.size();j++)
			{new_subtree.get(i).tree_branchs.get(j).children_id=new_subtree.get(i).tree_branchs.get(j).children_id+last_item;
			}
			}

			for (int i=0;i<new_subtree.size();i++)
			{
				Globals.Pruned_Tree_Nodes.add(new_subtree.get(i));
				Globals.Item_Location.add(Globals.Pruned_Tree_Nodes.size()-1);
			}

			return 1+last_item;





		}
		else
		{
			this_node=Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(Node_Id));

			int p_id=this_node.parent_id;
			int p_id_causing_branch=0;
			for (int i=0;i<Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(p_id)).tree_branchs.size();i++)
			{
				if (this_node.id==Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(p_id)).tree_branchs.get(i).children_id)
				{
					p_id_causing_branch=i;
					break;
				}
			}
			Vector<Node> temp_subtree=Get_Subtree_inc_header(this_node);
			PWC45.Remove_Subtree(temp_subtree);
			int last_item=Globals.Item_Location.size()-1;

			//for the first node
			new_subtree.get(0).id=1+last_item;
			Globals.Pruned_Tree_Nodes.get(Globals.Item_Location.get(p_id)).tree_branchs.get(p_id_causing_branch).children_id=new_subtree.get(0).id;

			new_subtree.get(0).parent_id=p_id;
			for (int j=0;j<new_subtree.get(0).tree_branchs.size();j++)
			{new_subtree.get(0).tree_branchs.get(j).children_id=new_subtree.get(0).tree_branchs.get(j).children_id+last_item;
			}

			//for all other nodes
			for (int i=1;i<new_subtree.size();i++)
			{   new_subtree.get(i).id=new_subtree.get(i).id+last_item;
			new_subtree.get(i).parent_id=new_subtree.get(i).parent_id+last_item;
			for (int j=0;j<new_subtree.get(i).tree_branchs.size();j++)
			{new_subtree.get(i).tree_branchs.get(j).children_id=new_subtree.get(i).tree_branchs.get(j).children_id+last_item;
			}
			}

			for (int i=0;i<new_subtree.size();i++)
			{
				Globals.Pruned_Tree_Nodes.add(new_subtree.get(i));
				Globals.Item_Location.add(Globals.Pruned_Tree_Nodes.size()-1);
			}

			return 1+last_item;



		}


	}


	public static void Update_Item_Location(int removed_item_index, Vector<Integer> Item_Location)
	{   
		Integer start=removed_item_index;
		for (int i=start+1; i<Item_Location.size();i++)
		{
			Item_Location.set(i, Item_Location.get(i)-1);
		}
		Item_Location.set(removed_item_index,-1);
	}//end function Update_Item_Location()

	public static void Update_Item_Location_intheNodes(Vector<Node> Pruned_Tree_Nodes, Vector<Integer> Item_Location)
	{   

		for (int i=0; i<Pruned_Tree_Nodes.size();i++)
		{

			Pruned_Tree_Nodes.get(i).id=Item_Location.get(Pruned_Tree_Nodes.get(i).id)+1;
			if (Pruned_Tree_Nodes.get(i).parent_id!=0)
			{
				Pruned_Tree_Nodes.get(i).parent_id=Item_Location.get(Pruned_Tree_Nodes.get(i).parent_id)+1;
			}
			for (int j=0;j<Pruned_Tree_Nodes.get(i).tree_branchs.size();j++)
			{
				Pruned_Tree_Nodes.get(i).tree_branchs.get(j).children_id=Item_Location.get(Pruned_Tree_Nodes.get(i).tree_branchs.get(j).children_id)+1;
			}
		}

	}//end function Update_Item_Location_intheNodes()

}