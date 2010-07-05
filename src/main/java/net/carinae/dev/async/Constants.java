/*
 * Copyright 2010 Carlos Vara
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.carinae.dev.async;

/**
 * Constant values used in the async executor.
 * 
 * @author Carlos Vara
 */
public class Constants {

    private Constants() {
        // No instances please
    }
    
    public static final long TASK_RUNNER_RATE = 60l*1000l; // Every minute
    public static final long TASK_HYPERVISOR_RATE = 60l*60l*1000l; // Every hour
    
}
