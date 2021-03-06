/*
 *
 *  Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.flipkart.fdp.migration.distcp.state;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.fs.Path;

import com.flipkart.fdp.migration.distcp.config.DCMConstants.Status;

public interface StateManager extends Closeable {

	public void beginBatch() throws IOException;

	public void completeBatch(Status status) throws IOException;

	public void updateTransferStatus(TransferStatus status) throws IOException;

	public Map<String, TransferStatus> getTransferStatus(String taskId)
			throws IOException;

	public void savePreviousTransferStatus(
			Map<String, TransferStatus> prevState) throws IOException;

	public Map<String, TransferStatus> getPreviousTransferStatus()
			throws IOException;

	public Path getReportPath();

	public String getRunId();
}
