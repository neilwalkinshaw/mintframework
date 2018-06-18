/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.model.statepair;


import mint.inference.efsm.scoring.scoreComputation.Score;

public class OrderedStatePairWithScore extends OrderedStatePair implements Comparable {
	
	
	protected Score score;

	public OrderedStatePairWithScore(Integer a, Integer b){
		super(a,b);
		score = new Score(0);
	}
	
	public OrderedStatePairWithScore(Integer a, Integer b, Score score){
		super(a,b);
		this.score = score;
	}


	public void setScore(Score score){
		this.score = score;
	}


	public Score getScore(){
		return score;
	}
	

	@Override
	public int compareTo(Object arg0) {
		int retScore = -1;
        if(arg0 instanceof OrderedStatePairWithScore){

			OrderedStatePairWithScore sp = (OrderedStatePairWithScore)arg0;
			retScore = sp.getScore().compareTo(score);

		}
		return retScore;
	}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OrderedStatePairWithScore){
            OrderedStatePairWithScore other = (OrderedStatePairWithScore) obj;
            if(super.equals(other))
                return other.getScore().compareTo(score) ==0;

        }
        return false;
    }

    @Override
    public String toString() {
        return "("+a +","+b+") - score: "+score;
    }
}
