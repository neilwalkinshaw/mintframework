/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package mint.testgen.stateless.gp;

import com.microsoft.z3.Z3Exception;
import org.apache.log4j.BasicConfigurator;
import mint.tracedata.types.VariableAssignment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class GPTestRunnerTest {

	Set<VariableAssignment<?>> vars;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void complexTest() throws Z3Exception {
		//GPTestRunner gpt = new GPTestRunner()
	}
	

	

}
