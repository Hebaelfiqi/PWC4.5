import java.util.Vector;

public final  class Multiple_Variables {
	private final String Most_Frequent_class;
	private final Double Error;

	public Multiple_Variables (String Most_Frequent_class, Double Error) {
		this.Most_Frequent_class = Most_Frequent_class;
		this.Error = Error;
	}

	public String getMost_Frequent_class() {
		return Most_Frequent_class;
	}

	public Double getError() {
		return Error;
	}


	public static Multiple_Variables max_class(Vector<String> classes, Vector<Double> Weights) {
		Vector<String> disticive_classes = new Vector<String>();
		Vector<Double> disticive_classes_freq = new Vector<Double>();
		// initally add the class of first row, then loop to add the rest
		disticive_classes.add(classes.get(0));
		disticive_classes_freq.add(Weights.get(0));
		for (int i = 1; i < classes.size(); i++) {
			boolean exist = true;
			String Ci = classes.get(i);
			for (int j = 0; j < disticive_classes.size(); j++) {
				String comparingItem = disticive_classes.get(j);
				if (Ci.equalsIgnoreCase(comparingItem)) {
					exist = true;
					Double temp = disticive_classes_freq.get(j) + Weights.get(i);
					disticive_classes_freq.set(j, temp);
					break;
				} else {
					exist = false;
				}

			}
			if (!exist) {
				disticive_classes.add(Ci);
				disticive_classes_freq.add(Weights.get(i));
			}
		}



		Double maxValue=0.0; 
		String Most_Frequent_class="";

		String[] loc_classes_names=Globals.classes_names;  //initialize best_class
		String selected_class="p1\u2192"+loc_classes_names[0]+", p2\u2192"+loc_classes_names[1].substring(0,loc_classes_names[1].length()-1);
		for (int i=0;i<disticive_classes.size();i++) //find its location and frequency
		{
			if (disticive_classes.get(i).equals(selected_class))
			{
				maxValue=disticive_classes_freq.get(i);
				Most_Frequent_class=disticive_classes.get(i);
				break;
			}
		}



		for (int i = 0; i < disticive_classes_freq.size(); i++) {
			if (disticive_classes_freq.get(i) > maxValue) {
				maxValue = disticive_classes_freq.get(i);
				Most_Frequent_class = disticive_classes.get(i);
			}
		}

		Double Errors=0.0;
		for (int i = 0; i < disticive_classes_freq.size(); i++) 
		{ boolean rev_cond=disticive_classes.get(i).equals(Most_Frequent_class);
		if (!rev_cond )
		{	
			Errors =Errors+ disticive_classes_freq.get(i);

		}
		}
		return new Multiple_Variables(Most_Frequent_class,Errors);
	}
}