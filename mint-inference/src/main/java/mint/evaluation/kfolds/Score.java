/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.evaluation.kfolds;

public class Score {
	
	protected double tp, tn, fp, fn, sensitivity=0D,specificity=0D,bcr=0D, hbcr = 0D, kappa = 0D;
	protected long duration = 0L;

	public Score(double tp, double tn, double fp, double fn){
		this.tp = tp;
		this.tn = tn;
		this.fp = fp;
		this.fn = fn;
		sensitivity = tp/(tp + fn);
		specificity = tn / (tn + fp);
		computeBCRs();
		kappa = computeKappa();
	}
	

	private double computeKappa(){
		double observed = (tp+tn)/(tp+tn+fp+fn);
		double t = (tp+tn)/(tp+tn+fp+fn);
		double f = (tp+fp)/(tp+tn+fp+fn);
		double expected = (t+f)/(tp+tn+fp+fn);
		return (observed - expected)/(1-expected);
	}

	public void setKappa(double kappa){
		this.kappa = kappa;
	}

	public double getKappa() {

		return kappa;
	}

	private void computeBCRs(){
		if(!(sensitivity == 0 || specificity == 0))
			hbcr = (2*sensitivity*specificity)/(sensitivity + specificity);
		bcr = (sensitivity+specificity)/2;
	}

	

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getSensitivity() {
		return sensitivity;
	}

	public double getSpecificity() {
		return specificity;
	}

	public double getBCR() {
		return bcr;
	}
	
	public double getHarmonicBCR() {
		return hbcr;
	}

	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}

	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	public void setBCR(double bcr) {
		this.bcr = bcr;
	}

	public String toString(){
		return sensitivity +", "+specificity+", "+kappa;
	}

}