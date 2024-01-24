/*
 * Copyright 2024 tison <wander4096@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tisonkun.os.core;

public enum Arch {
    x86_64,
    x86_32,
    itanium_64,
    itanium_32,
    sparc_32,
    sparc_64,
    arm_32,
    aarch_64,
    mips_32,
    mipsel_32,
    mips_64,
    mipsel_64,
    ppc_32,
    ppcle_32,
    ppc_64,
    ppcle_64,
    s390_32,
    s390_64,
    riscv,
    riscv64,
    e2k,
    loongarch_64,
    unknown;

    public boolean isUnknown() {
        return this == unknown;
    }
}
