
public class Pair <U, V> { 
	//this class is used to overcome the limitation of java to return more than single variable as a function output

	/**
	 * The first element of this <code>Pair</code>
	 */
	private U first;



	/**
	 * The second element of this <code>Pair</code>
	 */
	private V second;

	/**
	 * Constructs a new <code>Pair</code> with the given values.
	 * 
	 * @param first  the first element
	 * @param second the second element
	 */
	public Pair(U first, V second) {

		this.first = first;
		this.second = second;
	}
	//getter for first and second

	public U getFirst() {
		return first;
	}


	public V getSecond() {
		return second;
	}

	public void setFirst(U first) {
		this.first = first;
	}

	public void setSecond(V second) {
		this.second = second;
	}

}
