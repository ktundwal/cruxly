package com.cruxly.lib.analytics;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cruxly.lib.analytics.TextSegment.SpecialSymbolSegment;

public class FiniteStateMachine {

	@Override
	public String toString() {
		return "FiniteStateMachine [ACCEPT_STATE=" + ACCEPT_STATE
				+ ", REJECT_STATE=" + REJECT_STATE + ", startState="
				+ startState + ", aliases=" + aliases + ", stateCount="
				+ stateCount + "]";
	}

	private static Logger LOGGER = Logger.getLogger(FiniteStateMachine.class.getName());
	
	public static final int ACCEPT = 1;
	public static final int REJECT = -1;

	protected final State ACCEPT_STATE = new State();
	protected final State REJECT_STATE = new State();

	protected final static String ANY = "*";
	protected final static String EPSILON = "__EPSILON__";

	public final static char ALIAS = '_';

	protected final static char REPEAT_NA = '1';
	protected final static char REPEAT_ONE_OR_MORE = '+';
	protected final static char REPEAT_ZERO_OR_MORE = '*';
	protected final static char REPEAT_ZERO_OR_ONE = '?';
	protected final static char REPEAT_ZERO = '^';

	private static final boolean ENABLE_MULTIWORD_ALIAS_FIX = true;
	
	public int MAX_RECURSION_DEPTH = 50;
	
	protected State startState;
	protected Hashtable<String, Set> aliases;

	protected int stateCount = 0;

	// For debugging
	protected static String lastPath = null;
	
	public FiniteStateMachine() {
		startState = new State();
		aliases = new Hashtable<String, Set>();
	}
	
	public void addAlias(String key, String [] values) {
		Set s = new Set();
		StringBuffer b = new StringBuffer();
		
		for (int i=0; i<values.length; i++) {
			if (values[i].charAt(0) == ALIAS)
				throw new RuntimeException ("Nested aliases are not supported");
			
			s.addElement(values[i]);
			b.append(values[i] + ", ");
		}
		
		aliases.put(key, s);
		if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Added alias: " + key + ": " + b.toString());
		}
	}
	
	public String [] resolveAlias(String aliasKey) {
		if (!aliases.containsKey(aliasKey))
			throw new RuntimeException ("Invalid alias: "+aliasKey);
		
		Set s = (Set)aliases.get(aliasKey);
		String[] result = new String[s.size()];
		s.copyInto(result);
		return result;
	}
	
	public boolean isAliasContains(String aliasKey, String key) {
		if (aliases.containsKey(aliasKey)) {
			Set lookup = (Set)aliases.get(aliasKey);
			return lookup.contains(key);
		} 
		
		throw new RuntimeException ("Invalid Alias: "+aliasKey);
	}
	
	public void addPath(String [] sequence, boolean accept) {
		
		if ((sequence == null) || (sequence.length == 0)) 
			return;
		
		lastPath = StringUtils.join(sequence, '+');
		
		State prev = null;
		State next = accept?ACCEPT_STATE:REJECT_STATE;
		
		for (int i=(sequence.length-1); i>=0; i--) {
			String step = sequence[i];
			
			if (i == 0) {
				prev = startState;
			} else {
				prev = new State();
				prev.endStateOnPath = accept?ACCEPT:REJECT;
			}
			
			char repeat = REPEAT_NA;
			boolean negation = false;
			
			while (step.length() > 1) {
				if (step.charAt(step.length()-1) == REPEAT_ZERO_OR_MORE) {
					step = step.substring(0, step.length()-1);
					repeat = REPEAT_ZERO_OR_MORE;
				} else if (step.charAt(step.length()-1) == REPEAT_ONE_OR_MORE) {
					step = step.substring(0, step.length()-1);
					repeat = REPEAT_ONE_OR_MORE;
				} else if (step.charAt(step.length()-1) == REPEAT_ZERO_OR_ONE) {
					step = step.substring(0, step.length()-1);
					repeat = REPEAT_ZERO_OR_ONE;
				} else if (step.charAt(step.length()-1) == REPEAT_ZERO) {
					step = step.substring(0, step.length()-1);
					negation = true;
				} else {
					break;
				}
			}
			
			// Resolve aliases
			String val = step;
			boolean alias = (step.charAt(0) == ALIAS);
			if (alias) {
				val = step.substring(1);
				if (!aliases.containsKey(val))
					throw new RuntimeException ("Invalid alias: "+val);
			
			} 
			
			if ((repeat == REPEAT_NA) || (repeat == REPEAT_ONE_OR_MORE) || (repeat == REPEAT_ZERO_OR_ONE)) {
				if (negation) {
					if (alias)
						prev.addTransitionNegAlias(val, next);
					else
						prev.addTransitionNeg(val, next);
				} else {
					if (alias)
						prev.addTransitionAlias(val, next);
					else
						prev.addTransition(val, next);
				}
			} 
			
			
			if ((repeat == REPEAT_ZERO_OR_MORE) || (repeat == REPEAT_ONE_OR_MORE)) {
				// put transition from node to itself
				if (negation) {
					if (alias)
						next.addTransitionNegAlias(val, next);
					else
						next.addTransitionNeg(val, next);
				} else {
					if (alias)
						next.addTransitionAlias(val, next);
					else
						next.addTransition(val, next);
				}
			}
			
			

			if ((repeat == REPEAT_ZERO_OR_ONE) || (repeat == REPEAT_ZERO_OR_MORE)) {
				if (prev.equals(next)) {
					StringBuffer seq = new StringBuffer();
					for (int k=0; k<sequence.length; k++)
						seq.append(sequence[k]).append(" ");
					throw new RuntimeException ("Epsilon-transition from state to self, during sequence "+seq.toString());
				}
				prev.addTransition(EPSILON, next);
			}
			
			next = prev;
		}
	}

	public FSMResult processSequence(TextSegment [] sequence, int targetState) {
		return processSequence(sequence, 0, targetState);
	}
	
	public FSMResult processSequence(TextSegment [] sequence, int idx, int targetState) {
		if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "*"+sequence[idx].text.substring(sequence[idx].posStart, sequence[idx].posEnd));
		}
		Vector<FSMResult> v = new Vector<FSMResult>();
		
		processSequenceAux (sequence, idx, idx, startState, v, 1, targetState);
	
		FSMResult bestResult = new FSMResult (-1,0, 0, "");
		
		for (int i=0; i<v.size(); i++) { 
			FSMResult er = (FSMResult)v.elementAt(i);
			/*if ((er.length > bestResult.length) ||
				((er.length == bestResult.length) && (er.result == REJECT))) {
					bestResult = er;
			}*/
			if (er.length > bestResult.length)
				bestResult = er;
		}
		
		if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "processSequence FSMResult = " + bestResult.matchedPath);
		}
		return bestResult;
	}
	
	protected void processSequenceAux(TextSegment[] sequence, int startIdx, int idx, State state, Vector<FSMResult> evals, int numMatched, int targetState) {
		if (idx >= sequence.length) 
			return;
		
		if ((idx-startIdx) > MAX_RECURSION_DEPTH)
			return;
	
		State [] nextStates1 = null;
		State [] nextStates2 = null;
		State [] nextStates3 = null;
		State [] nextStates4 = null;
		
		if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "             " + state);
		}
		
		if (sequence[idx] instanceof SpecialSymbolSegment) {
			String specialSymbol = ((SpecialSymbolSegment)sequence[idx]).getSymbol();
			nextStates1 = state.processTransitionStrict(specialSymbol);
			
			processSequenceAux (sequence, startIdx, idx+1, state, evals, numMatched, targetState);
		}
		else {
			nextStates1 = state.processTransition(sequence[idx].toString());
			if (idx + 1 < sequence.length 
					&& !(sequence[idx + 1] instanceof SpecialSymbolSegment)) {
				String request_symbol_plus_1 = sequence[idx].toString() + " " + sequence[idx+1].toString();
				nextStates2 = state.processTransition(request_symbol_plus_1); // To support 2-word aliases
				if (LOGGER.isLoggable(Level.FINER)) {
		            LOGGER.log(Level.FINER, "             FSM...NEXTSTATES 2 MATCH = " + request_symbol_plus_1);
				}
			}
			
			if (idx + 2 < sequence.length 
					&& !(sequence[idx + 1] instanceof SpecialSymbolSegment) 
					&& !(sequence[idx + 2] instanceof SpecialSymbolSegment)) {
				String request_symbol_plus_2 = sequence[idx].toString() + " " + 
					sequence[idx+1].toString() + " " + sequence[idx+2].toString();
				nextStates3 = state.processTransition(request_symbol_plus_2); // to support 3-word aliases
				if (LOGGER.isLoggable(Level.FINER)) {
		            LOGGER.log(Level.FINER, "             FSM...NEXTSTATES 3 MATCH = " + request_symbol_plus_2);
				}
			}
			
			if (idx + 3 < sequence.length 
					&& !(sequence[idx + 1] instanceof SpecialSymbolSegment) 
					&& !(sequence[idx + 2] instanceof SpecialSymbolSegment)
					&& !(sequence[idx + 3] instanceof SpecialSymbolSegment)) {
				String request_symbol_plus_3 = sequence[idx].toString() + " " + sequence[idx+1].toString() 
						+ " " + sequence[idx+2].toString() + " " + sequence[idx+3].toString();
				nextStates4 = state.processTransition(request_symbol_plus_3); // to support 4-word aliases
				if (LOGGER.isLoggable(Level.FINER)) {
		            LOGGER.log(Level.FINER, "             FSM...NEXTSTATES 4 MATCH = " + request_symbol_plus_3);
				}
			}
		}
		
		// Then merge all unique values in nextStates, nextStates2, nextStates3
		State[] nextStates = merge_states(nextStates1, nextStates2, nextStates3, nextStates4);
		
		//log.info("state.path(%s) idx(%d) numMatched(%d) targetState (%d)".format(
		//		state != null && state.path != null ? state.path : "", idx, numMatched, targetState));
		
		if (nextStates != null) {
			for (int i=0; i<nextStates.length; i++) {
				if (LOGGER.isLoggable(Level.FINER)) {
		            LOGGER.log(Level.FINER, String.format("                 i=%d nextStates[i]=%s", i, nextStates[i]));
				}
				if (LOGGER.isLoggable(Level.FINER)) {
		            LOGGER.log(Level.FINER, String.format("                 %s", this));
				}
				if ((nextStates[i] == ACCEPT_STATE) && (targetState == ACCEPT)) {
					if (LOGGER.isLoggable(Level.FINER)) {
			            LOGGER.log(Level.FINER, "ACCEPT_STATE: " + state.path);
					}
					evals.addElement(new FSMResult(numMatched, ACCEPT, idx, 
							(state != null && state.path != null) ? state.path : ""));
				} else if ((nextStates[i] == REJECT_STATE) && (targetState == REJECT)) {
					if (LOGGER.isLoggable(Level.FINER)) {
			            LOGGER.log(Level.FINER, "REJECT_STATE: " + state.path);
					}
					evals.addElement(new FSMResult(numMatched, REJECT, idx,
							(state != null && state.path != null) ? state.path : ""));
				} else if (nextStates[i].endStateOnPath == targetState) {
					int wordsToJump = idx+1;
					if (ENABLE_MULTIWORD_ALIAS_FIX) {
						if (nextStates2 != null) {
							wordsToJump = idx+2;
						}
						if (nextStates3 != null) {
							wordsToJump = idx+3;
						}
						if (nextStates4 != null) {
							wordsToJump = idx+4;
						}
					}
					processSequenceAux (sequence, startIdx, wordsToJump, nextStates[i], evals, numMatched+1, targetState);
				}
			}
		}
		
		if (!(sequence[idx] instanceof SpecialSymbolSegment)) {
			// Process epsilon transitions, these do not advance the pointer
			nextStates = state.getEpsilonTransitions();
			if (nextStates != null) {
				for (int i=0; i<nextStates.length; i++) {
					if ((nextStates[i] == ACCEPT_STATE) && (targetState == ACCEPT)) {
						if (LOGGER.isLoggable(Level.FINER)) {
				            LOGGER.log(Level.FINER, "ACCEPT_STATE: " + state.path);
						}
						evals.addElement(new FSMResult(numMatched, ACCEPT, idx,
								(state != null && state.path != null) ? state.path : ""));
					} else if ((nextStates[i] == REJECT_STATE) && (targetState == REJECT)) {
						if (LOGGER.isLoggable(Level.FINER)) {
				            LOGGER.log(Level.FINER, "REJECT_STATE: " + state.path);
						}
						evals.addElement(new FSMResult(numMatched, REJECT, idx,
								(state != null && state.path != null) ? state.path : ""));
					} else if (nextStates[i].endStateOnPath == targetState) {
						processSequenceAux (sequence, startIdx, idx+0, nextStates[i], evals, numMatched+1, targetState);
					}
				}	
			}
		}
	}

	private State[] merge_states(State[] nextStates, State[] nextStates2,
			State[] nextStates3, State[] nextStates4) {
		State[] mergedArray = nextStates;
		try {
			mergedArray = merge(nextStates, nextStates2, nextStates3, nextStates4);
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
	            LOGGER.log(Level.SEVERE, "Exception merging states: " + e.getMessage());
			}
		}
		return mergedArray;
	}
	
	public static State[] merge(State[]... arrays) {    
		LinkedList<State> list = new LinkedList<State>();
	    for(State[] array : arrays) {
	    	if (array != null) {
		        for(State t : array) {
		        	if (!listContainsState(list, t)) {
		        		list.add(t);
		        	}
		        }
	    	}
	    }
	    State[] empty = {};
	    return (State[])list.toArray(empty);
	}

	private static boolean listContainsState(List<State> list, State state) {
		for (State listItem : list) {
    		if (listItem.id == state.id) return true;
    	}
		return false;
	}

	public static class FSMResult {
		public final int result;
		public final int length;
		public final int lastIdx ;
		public final String matchedPath;
		
		public FSMResult (int l, int r, int li, String path) {
 			length = l;
			result = r;
			lastIdx = li;
			matchedPath = path;
		}
	}
	
	@SuppressWarnings("serial")
	protected class Transitions extends Hashtable<String, Set> {

		@Override
		public String toString() {
			StringBuffer b = new StringBuffer();
			
			Enumeration<String> enumVar = this.keys();
			while (enumVar.hasMoreElements()) {
				String aliasName = enumVar.nextElement();
				b.append(aliasName + "=");
				b.append((Set)aliases.get(aliasName) + ", ");
			}
			return "Transitions [ " + b.toString() + " ]";
		}
		
	}
	
	protected class State {

		protected Transitions transitions;
		protected Transitions transitionsNeg;
		protected Transitions transitionsAlias;
		protected Transitions transitionsNegAlias;

		protected final int id;
		protected final String path;

	    protected int endStateOnPath = 0;
		
		public State () {
			id = ++stateCount;
			LOGGER.finer ("State: "+stateCount);
			transitions = new Transitions();
			transitionsNeg = new Transitions();
			transitionsNeg.put(ANY, new Set());

			transitionsAlias = new Transitions();
			transitionsNegAlias = new Transitions();
			transitionsNegAlias.put(ANY, new Set());
			
			path = FiniteStateMachine.lastPath;
		}
		
		public boolean equals(Object o) {
			return ((o instanceof State) && (((State)o).id == this.id));
		}
		
		public int hashCode() {
			return id;
		}
		
			
		public void addTransition(String symbol, State state) {
			if (transitions.containsKey(symbol)) {
				Set v = (Set)transitions.get(symbol);
				v.addElement(state);
			} else {
				Set v = new Set();
				v.addElement(state);
				transitions.put(symbol, v);
			}
		}
		
		public void addTransitionAlias(String symbol, State state) {
			// If alias is small, resolve immediately
			Set resolve = (Set) aliases.get(symbol);
			if (resolve.size() <= 10) {
				for (Enumeration<String> enumVar = resolve.elements(); enumVar.hasMoreElements(); )
					addTransition(enumVar.nextElement(), state);
				return;
			}
			
			if (transitionsAlias.containsKey(symbol)) {
				Set v = (Set)transitionsAlias.get(symbol);
				v.addElement(state);
			} else {
				Set v = new Set();
				v.addElement(state);
				transitionsAlias.put(symbol, v);
			}
		}
		
		public void addTransitionNeg(String symbol, State state) {
			Set v = (Set)transitionsNeg.get(ANY);
			v.addElement(state);
			
			if (transitionsNeg.containsKey(symbol)) {
				v = (Set)transitionsNeg.get(symbol);
				v.addElement(state);
			} else {
				v = new Set();
				v.addElement(state);
				transitionsNeg.put(symbol, v);
			}
		}
		

		public void addTransitionNegAlias(String symbol, State state) {
			Set resolve = (Set) aliases.get(symbol);
		
			if (resolve.size() <= 10) {
				Set v = (Set)transitionsNeg.get(ANY);
				v.addElement(state);
				
				for (Enumeration<String> enumVar = resolve.elements(); enumVar.hasMoreElements(); )
					addTransitionNeg(enumVar.nextElement(), state);
				return;
			}
			
			Set v = (Set)transitionsNegAlias.get(ANY);
			v.addElement(state);
			
			if (transitionsNegAlias.containsKey(symbol)) {
				v = (Set)transitionsNegAlias.get(symbol);
				v.addElement(state);
			} else {
				v = new Set();
				v.addElement(state);
				transitionsNegAlias.put(symbol, v);
			}
		}
		
		public State[] getEpsilonTransitions() {
			if (transitions.containsKey(EPSILON)) {
				Set s = (Set)transitions.get(EPSILON);
				State [] result = new State[s.size()];
				s.copyInto(result);
				return result;
			}
			return null;
		}
		
		public State[] processTransitionStrict(String symbol) {
			Set result = new Set(100);
			
			if (transitions.containsKey(symbol)) {
				Set v = (Set)transitions.get(symbol);
				result.addAll(v);
			}
			
			Enumeration<String> enumVar = transitionsAlias.keys();
			while (enumVar.hasMoreElements()) {
				String aliasName = enumVar.nextElement();
				Set aliasVals = (Set)aliases.get(aliasName);
				if (aliasVals.contains(symbol)) {
					Set v = (Set)transitionsAlias.get(aliasName);
					result.addAll(v);			
				}
			}
			
			State[] _result = new State[result.size()];
			result.copyInto(_result);
			return _result;
		}
			
		public State[] processTransition(String symbol) {
			Set result = new Set(500);
			
			//logger.info(String.format("\n%s \n\t-> %s \n\t->  TRANSITIONS_ALIAS(%s)", 
			//	symbol, transitions, transitionsAlias));
			
			if (transitions.containsKey(ANY)) {
				Set v = (Set)transitions.get(ANY);
				result.addAll(v);
			}

			if (transitions.containsKey(symbol)) {
				Set v = (Set)transitions.get(symbol);
				//logger.info(String.format("\n\t\t'%s' is in '%s'. Adding all.", symbol, v));
				result.addAll(v);
			}
			
			Enumeration<String> enumVar = transitionsAlias.keys();
			while (enumVar.hasMoreElements()) {
				String aliasName = enumVar.nextElement();
				Set aliasVals = (Set)aliases.get(aliasName);
				if (aliasVals.contains(symbol)) {
					Set v = (Set)transitionsAlias.get(aliasName);
					result.addAll(v);			
				}
			}
			
			
			Set v = (Set)transitionsNeg.get(ANY);
			Set n = (Set)transitionsNeg.get(symbol);
			if (n == null) {
				result.addAll(v);
			} else {
				Set vn = v.minus(n);
				result.addAll(vn);
			}
			
			
			v = (Set)transitionsNegAlias.get(ANY);
			enumVar = transitionsNegAlias.keys();
			while ((v.size() > 0) && (enumVar.hasMoreElements())) {			
				String aliasName = (String)enumVar.nextElement();
				if (aliasName.equals(ANY))
					continue;

				Set aliasVals = (Set)aliases.get(aliasName);
				if (aliasVals.contains(symbol)) {
					n = (Set)transitionsNegAlias.get(aliasName);
					v = v.minus(n);
				}
			}
			result.addAll(v);
			
			
			
			if (result.size() == 0) {
				if (LOGGER.isLoggable(Level.FINER)) {
					StringBuffer b = new StringBuffer();
					b.append(String.format("State with endStateOnPath [%d] id [%d] cannot be transitioned any further for [%s]", 
							this.endStateOnPath, this.id, symbol));
					LOGGER.log(Level.FINER, b.toString());
				}
				return null;
			}
			
			State[] _result = new State[result.size()];
			result.copyInto(_result);
			
			if (LOGGER.isLoggable(Level.FINER)) {
				StringBuffer b = new StringBuffer();
				b.append(String.format("State with endStateOnPath [%d] id [%d] and can be transitioned to [%d] states for [%s]", 
						this.endStateOnPath, this.id, result.size(), symbol));
				for (State s : _result) {
					b.append("\n\t" + s.transitions);
				}
				LOGGER.log(Level.FINER, b.toString());
			}
			
			return _result;
		}
		
		@Override
		public String toString() {
			return "State [id=" + id + ", path=" + path + ", endStateOnPath="
					+ endStateOnPath + ", transitions=" + transitions
					+ ", transitionsNeg=" + transitionsNeg
					+ ", transitionsAlias=" + transitionsAlias
					+ ", transitionsNegAlias=" + transitionsNegAlias + "]";
		}
	}
}
