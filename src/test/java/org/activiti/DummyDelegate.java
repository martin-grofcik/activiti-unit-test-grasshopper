/*
 *  Copyright 2015: Thomson Reuters. All Rights Reserved.
 *  Proprietary and Confidential information of Thomson Reuters. Disclosure, Use or
 *  Reproduction without the written authorization of Thomson Reuters is prohibited.
 */

package org.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.log4j.Logger;

public class DummyDelegate implements JavaDelegate {

		
	private static final Logger logger = Logger.getLogger(DummyDelegate.class);

	public void execute(DelegateExecution execution) throws Exception {
		

	}

}
