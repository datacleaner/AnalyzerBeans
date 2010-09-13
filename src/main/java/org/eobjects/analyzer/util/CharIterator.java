package org.eobjects.analyzer.util;

import java.util.ListIterator;

/**
 * An iterator (with additional helper methods) for characters. The iterator
 * does not support any of the mutating methods like add and remove.
 * 
 * @author Kasper Sørensen
 */
public class CharIterator implements ListIterator<Character> {

	private char[] _chars;
	private int _index = -1;

	public CharIterator(CharSequence charSequence) {
		if (charSequence == null) {
			_chars = new char[0];
		} else {
			_chars = charSequence.toString().toCharArray();
		}
	}

	public CharIterator(char[] chars) {
		if (chars == null) {
			_chars = new char[0];
		} else {
			_chars = chars;
		}
	}

	public void reset() {
		_index = -1;
	}

	public Character first() {
		_index = 0;
		return current();
	}

	public Character last() {
		_index = _chars.length - 1;
		return current();
	}

	public CharIterator subIterator(int fromIndex, int toIndex) {
		int length = toIndex - fromIndex;

		assert length > 0;

		char[] chars = new char[length];
		System.arraycopy(_chars, fromIndex, chars, 0, length);
		return new CharIterator(chars);
	}

	public boolean is(Character c) {
		if (c == null) {
			return false;
		}
		return c == current();
	}

	public boolean isLetter() {
		return Character.isLetter(current());
	}

	public boolean isDigit() {
		return Character.isDigit(current());
	}

	public boolean isWhitespace() {
		return Character.isWhitespace(current());
	}

	public boolean isUpperCase() {
		return Character.isUpperCase(current());
	}

	public boolean isLowerCase() {
		return Character.isLowerCase(current());
	}

	public boolean isDiacritic() {
		return StringUtils.isDiacritic(current());
	}

	@Override
	public boolean hasNext() {
		return _index + 1 < _chars.length;
	}

	@Override
	public Character next() {
		_index++;
		return current();
	}

	public int currentIndex() {
		return _index;
	}

	public Character current() {
		return _chars[_index];
	}

	@Override
	public boolean hasPrevious() {
		return _index > 0;
	}

	@Override
	public Character previous() {
		_index--;
		return current();
	}

	@Override
	public int nextIndex() {
		return _index + 1;
	}

	@Override
	public int previousIndex() {
		return _index - 1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Character e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Character e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return new String(_chars);
	}

	public int length() {
		return _chars.length;
	}
}
