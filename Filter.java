import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a tool to filter a given Map by customizable criteria.
 * <p>
 * To add a parameter to the filter, use the AddCriteria(key, comparator, value
 * to check against) method once for each criteria you would like to add.
 * <p>
 * You can modify whether results with no matching filter are returned by
 * calling IncludeMissingFields() and providing either a true (to include) or a
 * false (to exclude).
 * <p>
 * To check for a match against the filters, use the .matches() method on the
 * Map object you'd like to test against
 * 
 * 
 * @author Anthony Knight
 *
 */
public class Filter {

	HashMap<String, ArrayList<Criteria>> criterion;
	private boolean allowMissingFields = true;

	public Filter() {
		InitializeNewCriterionArray();
	}

	/**
	 * Removes all criteria from the filter and resets to the default state.
	 */
	public void resetFilter() {
		InitializeNewCriterionArray();
	}

	/**
	 * If set to true, the filter will filter inclusively, functioning as an OR
	 * statement between criteria
	 * <p>
	 * If set to false, the filter will filter exclusively, functioning as an
	 * AND statement between criteria
	 * 
	 * @param allow
	 *            True = inclusive search/OR, false exclusive search/AND.
	 */
	public void allowOmittedFields(boolean allow) {
		allowMissingFields = allow;
	}

	private void InitializeNewCriterionArray() {
		criterion = new HashMap<String, ArrayList<Criteria>>();
	}
	
	public ArrayList<String> getCurrentFilterCriteriaAsString(){
		ArrayList<String> criteriaAsStrings = new ArrayList<String>();
		
		for (ArrayList<Criteria> criteriaList : criterion.values()) {
			for (Criteria criteria : criteriaList) {
				criteriaAsStrings.add(criteria.toString());
			}
		}
		
		return criteriaAsStrings;		
	}
	
	public void makeNewFilterCriteriaFromString(){
		//code here not implemented
	}

	/**Adds a new parameter to the current filter using the given constraints. All parameters should 
	 * be able to be logically read from left to right.
	 * <p>
	 * e.g. addCriteria("age", Filter.ComparisonOperator.GREATER_THAN, "30") would be a filter criteria for an age greater than 30
	 * <p>
	 * <p>
	 * <code>
	 * 
	 * <i>EXISTS</i>evaluates 'true' to check if exists, or 'false' to check if doesn't exist.
	 * <p>
	 * 
	 * <i>GREATER_THAN</i> and <i>LESS_THAN</i> only accept values able to be parsed as Floats.
	 * 
	 * 
	 * 
	 * </code>
	 * @param key			A String matching a key in the user provided Map
	 * @param comparison	A logical operator from Filter.ComparisonOperator
	 * @param value			A String to compare the value of the user provided Map against
	 */
	public void addCriteria(String key, ComparisonOperator comparison, String value) {
		Criteria newCriteria = new Criteria(key, comparison, value);

		// if passed in value can't be parsed as a float then the Greater_Than
		// and Less_Than operators are invalid as we don't know if they want it 
		//lexicographically or alphabetically
		//with more time maybe add that choice for that option
		if (comparison.equals(ComparisonOperator.GREATER_THAN) || comparison.equals(ComparisonOperator.LESS_THAN)) {
			try {
				newCriteria.valueToCheckAsFloat = Float.parseFloat(value);
			} catch (Exception e) {
				//fail gracefully and just ignore the bad filter
				return;
			}
		}

		// made a map for criterion so that we can store multiple filter
		// criteria
		// for any given key
		// useful if we want to do something like age > 25 and age < 35
		if (criterion.get(key) != null) {
			// if they key has one or more Criteria associated with it, we add
			// another Criteria to its list
			criterion.get(key).add(newCriteria);
		} else {
			// otherwise we make a new list for the key and add the criteria
			ArrayList<Criteria> newCriteriaList = new ArrayList<Criteria>();
			newCriteriaList.add(newCriteria);
			criterion.put(key, newCriteriaList);
		}

	}

	/**
	 * Given an input Map<String, String> of keys and values to be tested
	 * against, each key is run through the added criterion to determine if its
	 * associated values are a match for the filter.
	 * 
	 * @param input
	 *            A Map<String, String> containing a set of keys and values to
	 *            be tested against
	 * @return boolean true if match for all filters, false if not a match for
	 *         all filters
	 */
	public boolean matches(Map<String, String> input) {
		boolean isMatch = true;

		if (criterion.size() == 0) {
			return false;
		}

		Set<String> criteriaKeys = criterion.keySet();
		// for each key that we have a filter criteria for
		for (String criteriaKey : criteriaKeys) {
			// if it's still potentially a match to the filter
			if (isMatch) {
				ArrayList<Criteria> criteriaToCheck = criterion.get(criteriaKey);
				// include based on inclusive/exclusive search choice
				if (!allowMissingFields && !criteriaToCheck.contains(criteriaKey)) {
					isMatch = false;
					continue;
				}

				//for each criteria that we have for that key, we check if it's still a valid match
				for (Criteria criteria : criteriaToCheck) {
					if (isMatch) {
						isMatch = checkValueAgainstCriteria(input.get(criteria.keyToCheck), criteria);
					}
				}
			}

		}

		//if it passed every filter then it is a match and returns true
		return isMatch;

	}

	//string values included with each to be used for string generation/parsing
	public static enum ComparisonOperator {
		EXISTS("exists"), GREATER_THAN("is greater than"), LESS_THAN("is less than"), EQUALS(
				"is equal to"), MATCHES_REGEX("matches the regular expression");
		private String value;

		private ComparisonOperator(String value) {
			this.value = value;
		}

		public String GetValue() {
			return value;
		}
	}

	private boolean checkValueAgainstCriteria(String value, Criteria criteria) {

		switch (criteria.comparisonOperator) {

		case EQUALS:
			//if filter is inclusive, this will include items with no key to compare
			if (value != null && value.equals(criteria.valueToCheck)) {
				return true;
			} else {
				return false;
			}

		case EXISTS:
			//return true if we find an item that we want to know if exists, or an item we want to know doesn't exist and doesn't
			if ((value != null && Boolean.valueOf(criteria.valueToCheck)) || (value == null && !Boolean.valueOf(criteria.valueToCheck))) {
				return true;
			} else {
				return false;
			}

		case GREATER_THAN:
			//only works on floats, would allow for better parsing with more time
			if (Float.parseFloat(value) > criteria.valueToCheckAsFloat) {
				return true;
			} else {
				return false;
			}

		case LESS_THAN:
			//only works on floats, would allow for better parsing with more time
			if (Float.parseFloat(value) < criteria.valueToCheckAsFloat) {
				return true;
			} else {
				return false;
			}

		case MATCHES_REGEX:
			if (value != null) {
				//make a regex pattern and matcher using the provided regex
				//then evaluate a match and return result
				Pattern regex = Pattern.compile(criteria.valueToCheck);
				Matcher matcher = regex.matcher(value);
				
				return matcher.matches();
			} else {
				return allowMissingFields;
			}

		default:
			break;

		}

		return false;
	}
}
