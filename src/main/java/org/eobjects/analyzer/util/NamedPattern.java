package org.eobjects.analyzer.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a useful abstraction over regular expressions where the groups are
 * named and not nescesarily ordered in the way specified by the enum that holds
 * the group names.
 * 
 * @author Kasper Sørensen
 */
public class NamedPattern<E extends Enum<E>> {

	public static final String SPECIAL_CHARACTERS= "æøåâäáàôöóòêëéèûüúùîïíìñńǹḿ";
	public static final String GROUP_LITERAL;
	
	static {
		StringBuilder sb = new StringBuilder("([a-zA-Z0-9");
		sb.append(SPECIAL_CHARACTERS.toLowerCase());
		sb.append(SPECIAL_CHARACTERS.toUpperCase());
		sb.append("]+)");
		GROUP_LITERAL = sb.toString();
	}

	private EnumMap<E, Integer> groupIndexes;
	private Pattern pattern;
	private Class<E> groupEnum;

	public NamedPattern(String pattern, Class<E> groupEnum) {
		if (pattern == null) {
			throw new IllegalArgumentException("pattern cannot be null");
		}
		if (groupEnum == null) {
			throw new IllegalArgumentException("groupEnum cannot be null");
		}

		pattern = pattern.replaceAll("\\(", "\\\\(");
		pattern = pattern.replaceAll("\\)", "\\\\)");
		pattern = pattern.replaceAll("\\[", "\\\\[");
		pattern = pattern.replaceAll("\\]", "\\\\]");

		this.groupEnum = groupEnum;

		groupIndexes = new EnumMap<E, Integer>(groupEnum);

		E[] availableGroupNames = groupEnum.getEnumConstants();

		List<E> usedGroupNames = new ArrayList<E>();
		List<Integer> groupNameStringIndexOfs = new ArrayList<Integer>();

		for (int i = 0; i < availableGroupNames.length; i++) {
			E group = availableGroupNames[i];
			String groupToken = getGroupToken(group);

			int indexOf = pattern.indexOf(groupToken);
			if (indexOf != -1) {
				usedGroupNames.add(group);
				groupNameStringIndexOfs.add(indexOf);
			}
		}

		if (usedGroupNames.isEmpty()) {
			throw new IllegalArgumentException("None of the groups defined in "
					+ groupEnum.getSimpleName()
					+ " where found in the pattern: " + pattern);
		}

		Integer groupIndex = getIndexOfHighest(groupNameStringIndexOfs);
		while (groupIndex != null) {

			E group = usedGroupNames.remove(groupIndex.intValue());
			groupNameStringIndexOfs.remove(groupIndex.intValue());

			groupIndexes.put(group, usedGroupNames.size() + 1);

			pattern = pattern.replace(getGroupToken(group), GROUP_LITERAL);

			groupIndex = getIndexOfHighest(groupNameStringIndexOfs);
		}

		this.pattern = Pattern.compile(pattern);
	}

	protected String getGroupToken(E group) {
		return group.name();
	}

	private Integer getIndexOfHighest(List<Integer> integerList) {
		Integer result = null;
		int highestValue = -1;
		for (int i = 0; i < integerList.size(); i++) {
			Integer integer = integerList.get(i);
			if (integer.intValue() > highestValue) {
				result = i;
				highestValue = integer;
			}
		}
		return result;
	}

	public NamedPatternMatch<E> match(String string) {
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			
			int start = matcher.start();
			int end = matcher.end();
			
			if (start == 0 && end == string.length()) {
				Map<E, String> resultMap = new EnumMap<E, String>(groupEnum);
				Set<Entry<E, Integer>> entries = groupIndexes.entrySet();
				for (Entry<E, Integer> entry : entries) {
					E group = entry.getKey();
					Integer groupIndex = entry.getValue();
					String result = matcher.group(groupIndex);
					resultMap.put(group, result);
				}
				return new NamedPatternMatch<E>(resultMap);
			}
		}

		return null;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
