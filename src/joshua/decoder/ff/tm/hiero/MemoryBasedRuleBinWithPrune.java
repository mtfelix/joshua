/* This file is part of the Joshua Machine Translation System.
 * 
 * Joshua is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package joshua.decoder.ff.tm.hiero;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import joshua.decoder.JoshuaConfiguration;
import joshua.decoder.Support;
import joshua.decoder.ff.FeatureFunction;
import joshua.decoder.ff.tm.Rule;

/**
 * Contain all rules with the same french side (and thus same arity). We should keep this code alive, even though currently nobody uses it; as we may want to do offline pruning of grammar.
 *
 * @author Zhifei Li, <zhifei.work@gmail.com>
 * @version $LastChangedDate$
 */
public class MemoryBasedRuleBinWithPrune	extends MemoryBasedRuleBin {
	static double EPSILON = 0.000001;
	static int IMPOSSIBLE_COST = 99999;//max cost
	
	protected double cutoff = IMPOSSIBLE_COST;//initial cost
	protected PriorityQueue<Rule> heapRules = null;//sort the rules based on the stateless cost
	
	public MemoryBasedRuleBinWithPrune(int arity, int[] sourceTokens) {
		super(arity, sourceTokens);
	}
	
	//TODO: bug in getSortedRules: even the l_models is new, we do not update the est cost for the rule, which is wrong
	/**
	 * TODO: now, we assume this function will be called
	 * only after all the rules have been read; this
	 * method need to be synchronized as we will call
	 * this function only after the decoding begins;
	 * to avoid the synchronized method, we should call
	 * this once the grammar is finished
	 * what about the weights changed as in MERT??
	 */
	//public synchronized ArrayList<Rule> get_sorted_rules() {
	public List<Rule> getSortedRules(ArrayList<FeatureFunction> l_models) {
		if (null != l_models || ! this.sorted) {
			//TODO: even the l_models is new, we do not update the est cost for the rule, which is wrong
			this.rules.clear();
			while (this.heapRules.size() > 0) {
				Rule t_r = (Rule) this.heapRules.poll();
				this.rules.add(0, t_r);
			}
			this.sorted    = true;
			this.heapRules = null;
		}
		return this.rules;
	}
	
	
	public void addRule(Rule rule) {
		if (null == this.heapRules) { // first time
			this.heapRules = new PriorityQueue<Rule>(1, Rule.NegtiveCostComparator); // TODO: initial capacity?
			this.arity = rule.getArity();
			this.sourceTokens = rule.getFrench();
		}
		if (rule.getArity() != this.arity) {
			Support.write_log_line(String.format("RuleBin: arity is not matching, old: %d; new: %d", this.arity, rule.getArity()),	Support.ERROR);
			return;
		}
		this.heapRules.add(rule);
		if (rule.getEstCost() + JoshuaConfiguration.rule_relative_threshold < this.cutoff) {
			this.cutoff = rule.getEstCost() + JoshuaConfiguration.rule_relative_threshold;
		}
		rule.setFrench(this.sourceTokens); //TODO: this will release the memory in each rule, but still have a pointer
	}
	
	
	protected int run_pruning() {
		int n_pruned = 0;
		while (this.heapRules.size() > JoshuaConfiguration.max_n_rules || this.heapRules.peek().getEstCost() >= this.cutoff) {
			n_pruned++;
			this.heapRules.poll();
			if (null == this.heapRules.peek()) {
				System.out.println("the stack is empty, which might be wrong; cutoff:" + this.cutoff);
			}
		}
		if (this.heapRules.size() == JoshuaConfiguration.max_n_rules) {
			this.cutoff =
				(this.cutoff < this.heapRules.peek().getEstCost())
				? this.cutoff
				: this.heapRules.peek().getEstCost() + EPSILON;//TODO
		}
		return n_pruned++;
	}
	
	
/* TODO Possibly remove - this method is never called.
	private void print_info(int level) {
		Support.write_log_line(
			String.format("RuleBin, arity is %d", this.arity),
			level);
		ArrayList<Rule> t_l = getSortedRules();
		for (int i = 0; i < t_l.size(); i++) {
			((Rule_Memory)t_l.get(i)).print_info(level);
		}
	}
*/
}