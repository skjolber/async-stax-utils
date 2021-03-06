/***************************************************************************
 * Copyright 2017 Thomas Rorvik Skjolberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.github.skjolber.asyncstaxutils.io;

import java.io.IOException;

import com.github.skjolber.asyncstaxutils.StreamProcessor;

public interface DelegateStreamCallback {

	/**
	 * Notify that streams were closed.
	 * 
	 * @param processor target
	 * @param success true if no {@linkplain IOException} or {@linkplain RuntimeException} occurred.
	 */
	
	void closed(StreamProcessor processor, boolean success);
	
}
