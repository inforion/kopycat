/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.elfloader.processors.mips.enums

import ru.inforion.lab403.common.extensions.*


 
enum class MipsDynamicSectionTag(val id: Int) {
    DT_MIPS_RLD_VERSION             (0x70000001),    // This member gives a version ID for the Runtime Linker Interface.
    DT_MIPS_TIME_STAMP              (0x70000002),    // This member gives a timestamp.
    DT_MIPS_ICHECKSUM               (0x70000003),    // This member gives a checksum of all external strings (names?) and common sizes.
    DT_MIPS_IVERSION                (0x70000004),    // This member gives the string table index of a compatible version string.
    DT_MIPS_FLAGS                   (0x70000005),    // This member contains MIPS-specific flags
    DT_MIPS_BASE_ADDRESS            (0x70000006),    // This member contains the base address assumed for the executable/DSO at static link time.
    DT_MIPS_MSYM                    (0x70000007),    //
    DT_MIPS_CONFLICT                (0x70000008),    // This member contains the address of the .conflict section (mandatory if there is a .conflict section).
    DT_MIPS_LIBLIST                 (0x70000009),    // This member contains the address of the .liblist section.
    DT_MIPS_LOCAL_GOTNO             (0x7000000a),    // This member contains the number of local GOT entries.
    DT_MIPS_CONFLICTNO              (0x7000000b),    // This member contains the number of entries in the .conflict section (mandatory if DT_MIPS_CONFLICT is present).
    DT_MIPS_LIBLISTNO               (0x70000010),    // This member contains the number of entries in the .liblist section. It is required if DT_MIPS_LIBLIST is present.
    DT_MIPS_SYMTABNO                (0x70000011),    // This member contains the number of entries in the .dynsym section.
    DT_MIPS_UNREFEXTNO              (0x70000012),    // Contains the index into the dynsymbol table of the first external symbol, not referenced in the same object.
    DT_MIPS_GOTSYM                  (0x70000013),    // Contains the index into the dynsymbol table of the first entry, corresponds to an external symbol with GOT-entry.
    DT_MIPS_HIPAGENO                (0x70000014),    // This member contains the number of page table entries in the GOT. It is used by profiling tools and is optional.
    DT_MIPS_RLD_MAP                 (0x70000016),    // Contains a writeable address for debugger use. An indirect version of DT_DEBUG
    DT_MIPS_DELTA_CLASS             (0x70000017),    //
    DT_MIPS_DELTA_CLASS_NO          (0x70000018),    //
    DT_MIPS_DELTA_INSTANCE          (0x70000019),    //
    DT_MIPS_DELTA_INSTANCE_NO       (0x7000001a),    //
    DT_MIPS_DELTA_RELOC             (0x7000001b),    //
    DT_MIPS_DELTA_RELOC_NO          (0x7000001c),    //
    DT_MIPS_DELTA_SYM               (0x7000001d),    //
    DT_MIPS_DELTA_SYM_NO            (0x7000001e),    //
    DT_MIPS_DELTA_CLASSSYM          (0x70000020),    //
    DT_MIPS_DELTA_CLASSSYM_NO       (0x70000021),    //
    DT_MIPS_CXX_FLAGS               (0x70000022),    //
    DT_MIPS_PIXIE_INIT              (0x70000023),    // This member contains the address of an initialization routine created by pixie.
    DT_MIPS_SYMBOL_LIB              (0x70000024),    // Contains the address of the .MIPS.symlib section, a map from the .dynsym symbols to the DSOs (optional)
    DT_MIPS_LOCALPAGE_GOTIDX        (0x70000025),    // This member contains the index in the GOT of the first page table entry for a segment.
    DT_MIPS_LOCAL_GOTIDX            (0x70000026),    // Contains the index in the GOT of the first entry for a local symbol. Mandatory with local symbol entries.
    DT_MIPS_HIDDEN_GOTIDX           (0x70000027),    // Contains the index in the GOT of the first entry for a hidden symbol. Mandatory with hidden symbol entries.
    DT_MIPS_PROTECTED_GOTIDX        (0x70000028),    // Contains the index in the GOT of the first entry for a protected symbol. Mandatory with protected symbol entries.
    DT_MIPS_OPTIONS                 (0x70000029),    // Contains the address of the Options section, containing various execution options. It is mandatory.
    DT_MIPS_INTERFACE               (0x7000002a),    // Contains the address of the .MIPS.interface section, describing subprogram interfaces
    DT_MIPS_DYNSTR_ALIGN            (0x7000002b),    //
    DT_MIPS_INTERFACE_SIZE          (0x7000002c),    // Contains the size in bytes of the .MIPS.interface section
    DT_MIPS_RLD_TEXT_RESOLVE_ADDR   (0x7000002d),    // Contains the link-time address of _rld_text_resolve to place in GOT entry 0.
    DT_MIPS_PERF_SUFFIX             (0x7000002e),    // This member contains an index to the string table.
    DT_MIPS_COMPACT_SIZE            (0x7000002f),    // Contains the size of a ucode compact relocation header record, and is not present in -n32 or -64 ELF files.
    DT_MIPS_GP_VALUE                (0x70000030),    // Contains the GP value of a specific GP relative range. Used with multigot and is dynamic table order sensitive.
    DT_MIPS_AUX_DYNAMIC             (0x70000031),    // This member contains the address of an auxiliary dynamic table in the case of multigot.
    DT_MIPS_DIRECT_SGI              (0x70000032),    // If exists it tells the runtime linker (rld) that the .symlib section is fully filled out and preemption is not used.
    DT_MIPS_PLTGOT_GNU              (0x70000032),    // The address of .got.plt in an executable using the new non-PIC ABI.
    DT_MIPS_RLD_OBJ_UPDATE          (0x70000033),    // This member gives the dynamic symbol entry of a callback function defined in this dso/a.out.
    DT_MIPS_RWPLT                   (0x70000034),    // The base of the PLT in an executable using the new non-PIC ABI if that PLT is writable.
    DT_MIPS_RLD_MAP_REL                (0x70000035);    // Address of run time loader map, used for debugging.

    companion object {
        fun getNameById(id: Int): String {
            val st = find<MipsDynamicSectionTag> { it.id == id }
            return st?.name ?: "Unknown dynamic segment tag id ${id.hex8}"
        }
    }
}