import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class Attribute {

	String type; //numeric or nominal
	private String name; //attribute name
	int att_id; //attribute id starting from A(0) to A(n-1)



	private Vector<Vector<String>> all_values; //All values of Ai
	private Vector<Vector<String>> selected_values; // The values of selected examples for Ai. This is a subset of all_values
	private Vector<Vector<Double>> num_values; //numerical values in case of numerical attribute. It contains the numerical values of variable selected_values. 


	public Vector<Double> weights=new Vector<Double>(); 
	private double selected_values_weights; 

	private Vector<Outcome> outcomes; // possible outcomes for this attribute for the selected examples.

	private double InfoxT;
	private double split_infox;

	private double Gain;
	private double Gain_ratio;


	public void computeOutcomes(Integer[] a_selected_ids, String[] a_classes, double info, Vector<Double> a_weights){

		// compute outcomes based on values of nominal attributes

		selected_values= new Vector<Vector<String>>();//Initialize selected_values variable
		selected_values= get_selected_Attribute_value(a_selected_ids,att_id);//set the subset of selected values based on the selected examples: a_selected_ids variable
		this.num_values=Convert_VVString_2_VVdouble(selected_values); //convert the string values into double.



		//			Search for the best decision attribute that has the highest gain ratio
		//			based on Pair-wise relationship. For each numerical attribute Vi:

		//			1. Create three possible nominal outcomes “Min",“Eq",“Max"
		//			2. Create three empty groups of samples for each of these new outcomes
		//			SMin = φ, SEq = φ and SMax = φ


		this.outcomes=new Vector<Outcome>(); //clear any stored outcomes
		Outcome Min=new Outcome();
		Min.value="min";
		Outcome Eq=new Outcome();
		Eq.value="eq";
		Outcome Max=new Outcome();
		Max.value="max";

		//			3. Induce the relationship by comparing Vi(p1) to the values of	−→V i
		//			Loop: For each PSj = (p1, p2) belongs to PS

		for (int k=0;k<num_values.size();k++) //
		{
			Vector<Double> one_pair=num_values.get(k);

			Double p1=one_pair.get(0);
			Double p2=one_pair.get(1);
			if (Math.min(p1,p2)==Math.max(p1,p2))
			{
				Eq.IdS.add(a_selected_ids[k]);
				Eq.classes.add(a_classes[k]);
				Eq.weights.add(a_weights.get(k));
			}
			else if (p1==Math.min(p1,p2))
			{
				Min.IdS.add(a_selected_ids[k]);
				Min.classes.add(a_classes[k]);
				Min.weights.add(a_weights.get(k));
			}
			else//max
			{
				Max.IdS.add(a_selected_ids[k]);
				Max.classes.add(a_classes[k]);
				Max.weights.add(a_weights.get(k));
			}	
		}


		this.outcomes.add(Min);
		if (Globals.all_possible_branches)
		{
			this.outcomes.add(Eq);
		}
		else
		{
			if (Eq.IdS.size()!=0)
			{this.outcomes.add(Eq);}
		}
		this.outcomes.add(Max);


		//4. Calculate the gain ratio based on splitting PS into {SMin, SEq, SMax}

		selected_values_weights=PWC45.calc_sum(a_weights);
		this.setOutcomes(outcomes);
		InfoxT= Info_x_T(outcomes);
		double info_debug;
		info_debug=Calc_InfoDebug(outcomes);
		split_infox=Split_Info_x_T(outcomes);

		/*  Check whether all values are unknown or the same  */

		if ( outcomes.size()==1) 
		{Gain= -Globals.Epsilon;}
		else
		{
			int ReasonableSubsets=0;
			/*  There must be at least two subsets with MINOBJS items  */
			for (int  i=0;i<outcomes.size(); i++)
			{
				if (PWC45.calc_sum(outcomes.get(i).weights)>=Globals.min_inst_4leaf)
				{ReasonableSubsets++;}

			}
			if ( ReasonableSubsets < 2 ) 
			{
				Gain= -Globals.Epsilon;
			}
			else
			{
				Gain=(info-(info_debug/selected_values_weights));
			}
		}
		if (split_infox==0)
		{Gain_ratio=-Globals.Epsilon; Gain=-Globals.Epsilon;}
		else {Gain_ratio=Gain/split_infox;}


	}




	private Vector<Vector<Double>> Convert_VVString_2_VVdouble(	Vector<Vector<String>> selected_values2) {
		//this function is used to convert Vector of Vector of String
		//to Vector of vector of double
		Vector<Vector<Double>> output=new Vector<Vector<Double>>();
		for (int i=0;i<selected_values2.size();i++)
		{
			Vector<Double> one_pair=new Vector<Double>();
			one_pair.add(Double.parseDouble(selected_values2.get(i).get(0)));
			one_pair.add(Double.parseDouble(selected_values2.get(i).get(1)));
			output.add(one_pair);	
		}
		return output;
	}


	void distinct( Integer[] selected_ids, Vector<Vector<String>> values, String[] classes, Vector<Double> t_weights){
		//this function updates the outcomes based on the current selected ids
		Vector<Vector<Double>> d_values= Convert_VVString_2_VVdouble(values);
		this.outcomes=new Vector<Outcome>(); //clear any stored outcomes

		Outcome Min=new Outcome();
		Min.value="min";
		Outcome Eq=new Outcome();
		Eq.value="eq";
		Outcome Max=new Outcome();
		Max.value="max";


		for (int k=0;k<d_values.size();k++)
		{
			Vector<Double> one_pair=d_values.get(k);

			Double[] V_k = {one_pair.get(0),one_pair.get(1)};

			if (Collections.min(Arrays.asList(V_k))==Collections.max(Arrays.asList(V_k)))//equal
			{
				Eq.IdS.add(selected_ids[k]);
				Eq.classes.add(classes[k]);
				Eq.weights.add(t_weights.get(k));
			}
			else if (one_pair.get(0)==Collections.min(Arrays.asList(V_k))) 
			{
				Min.IdS.add(selected_ids[k]);
				Min.classes.add(classes[k]);
				Min.weights.add(t_weights.get(k));
			}
			else//max
			{
				Max.IdS.add(selected_ids[k]);
				Max.classes.add(classes[k]);
				Max.weights.add(t_weights.get(k));
			}	
		}
		this.outcomes.add(Min);
		if (Globals.all_possible_branches)
		{
			this.outcomes.add(Eq);
		}
		else
		{
			if (Eq.IdS.size()!=0)
			{this.outcomes.add(Eq);}
		}
		this.outcomes.add(Max);

	}


	double Info_x_T(Vector<Outcome> T)
	{
		double out=0;

		double T_size=selected_values_weights;// return |T| the number of instances in this attribute now}
		for (Outcome Ti : T){
			if (Ti.IdS.size()!=0)
			{
				Pair<Vector<String>,Vector<Double>> classes_freq= PWC45.freqCi(Ti.classes,PWC45.Integer_Vector_toArray(Ti.IdS),Ti.weights); //return a list contains frequency of each class for this outcome
				Double[] TiCi =classes_freq.getSecond().toArray(new Double[classes_freq.getSecond().size()]);

				// this list will be send to compute the Entropy for it Info[Ti]
				double Ti_size=PWC45.calc_sum(Ti.weights);
				Ti.Ti_by_T=Ti_size/(double)T_size;
				Ti.info_Ti=PWC45.Entropy(TiCi);
				out=out+(Ti.Ti_by_T*Ti.info_Ti);
			}
		}
		return out;
	}


	double Calc_InfoDebug(Vector<Outcome> T)
	{
		double out=0;
		for (Outcome Ti : T){
			double Ti_size=PWC45.calc_sum(Ti.weights);

			out=out+(Ti_size*Ti.info_Ti);
		}
		return out;
	}


	double Split_Info_x_T(Vector<Outcome> T)
	{
		float out=0;
		float T_size=(float) selected_values_weights; // return |T| the number of instances in this attribute now

		for (Outcome Ti : T){

			// this list will be send to compute the Entropy for it Info[Ti]
			float Ti_size=(float) PWC45.calc_sum(Ti.weights);
			out= (out-((Ti_size/(float)T_size)*PWC45.log2(Ti_size/(float)T_size)));
		}
		return out;
	}



	public static Vector<Vector<String>> get_selected_Attribute_value (Integer[] selected_examples, int attribute_id){

		Vector<Vector<String>> values = new Vector<Vector<String>>();


		for(int i = 0 ; i < selected_examples.length;i++){
			values.add(Globals.Training_data.get(selected_examples[i]).get(attribute_id));
		}

		return values;
	}

	@Override
	public Object clone()  {
		try{
			Attribute cloned = (Attribute) super.clone();
			return cloned;
		}
		catch(CloneNotSupportedException e){
			System.out.println(e);
			return null;
		}

	}	 



	//getter and setter functions

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public  Vector<Outcome> getOutcomes() {
		return outcomes;
	}

	public  void setOutcomes(Vector<Outcome> outcomes) {
		this.outcomes = outcomes;
	}

	public Vector<Vector<Double>> getNum_values() {
		return num_values;
	}

	public void setNum_values(Vector<Vector<Double>> num_values) {
		this.num_values = num_values;
	}

	public double getInfoxT() {
		return InfoxT;
	}

	public void setInfoxT(double infoxT) {
		InfoxT = infoxT;
	}

	public double getGain() {
		return Gain;
	}

	public void setGain(double gain) {
		Gain = gain;
	}

	public Integer getAtt_id() {
		return att_id;
	}

	public void setAtt_id(Integer att_id) {
		this.att_id = att_id;
	}

	public Vector<Vector<String>> getAll_values() {
		return all_values;
	}

	public void setAll_values(Vector<Vector<String>> all_values) {
		this.all_values = all_values;
	}

	public double getGain_ratio() {
		return Gain_ratio;
	}

	public void setGain_ratio(double gain_ratio) {
		Gain_ratio = gain_ratio;
	}

	public double getSplit_infox() {
		return split_infox;
	}

	public void setSplit_infox(double split_infox) {
		this.split_infox = split_infox;
	}

	public double getSelected_values_weights() {
		return selected_values_weights;
	}

	public void setSelected_values_weights(double selected_values_weights) {
		this.selected_values_weights = selected_values_weights;
	}

	public Vector<Vector<String>> getSelected_values() {
		return selected_values;
	}

	public void setSelected_values(Vector<Vector<String>> selected_values) {
		this.selected_values = selected_values;
	}

	public void setAtt_id(int att_id) {
		this.att_id = att_id;
	}




}
