#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include "builtins.h"

int variant0() {
	int failed = 0;
	create_empty_file("empty3");
	FILE* v1 = fopen("empty3", "r");
	failed = 0;
	fputs("[1/72]fclose(FILE* (Empty file, \"r\"))\n", stdout);
	errno = 0;
	int v1132 = fclose(v1);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1132, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb3780260, 0xffffffff, 0, 0, 0xffffffff, "", v1, __LINE__);
	failed |= 0;
	return failed;
}

int variant1() {
	int failed = 0;
	create_random_file("random3");
	FILE* v2 = fopen("random3", "r");
	failed = 0;
	fputs("[2/72]fclose(FILE* (File with random text, \"r\"))\n", stdout);
	errno = 0;
	int v1133 = fclose(v2);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1133, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdd987260, 0xffffffff, 0, 0, 0xffffffff, "", v2, __LINE__);
	failed |= 0;
	return failed;
}

int variant2() {
	int failed = 0;
	remove_file("file4");
	FILE* v3 = fopen("file4", "w");
	failed = 0;
	fputs("[3/72]fclose(FILE* (File name, \"w\"))\n", stdout);
	errno = 0;
	int v1134 = fclose(v3);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1134, __LINE__);
	failed |= validate_file_pointer((FILE*)0xad912260, 0xffffffff, 0, 0, 0xffffffff, "", v3, __LINE__);
	failed |= 0;
	return failed;
}

int variant3() {
	int failed = 0;
	create_empty_file("empty4");
	FILE* v4 = fopen("empty4", "w");
	failed = 0;
	fputs("[4/72]fclose(FILE* (Empty file, \"w\"))\n", stdout);
	errno = 0;
	int v1135 = fclose(v4);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1135, __LINE__);
	failed |= validate_file_pointer((FILE*)0x84998260, 0xffffffff, 0, 0, 0xffffffff, "", v4, __LINE__);
	failed |= 0;
	return failed;
}

int variant4() {
	int failed = 0;
	create_random_file("random4");
	FILE* v5 = fopen("random4", "w");
	failed = 0;
	fputs("[5/72]fclose(FILE* (File with random text, \"w\"))\n", stdout);
	errno = 0;
	int v1136 = fclose(v5);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1136, __LINE__);
	failed |= validate_file_pointer((FILE*)0x056f7260, 0xffffffff, 0, 0, 0xffffffff, "", v5, __LINE__);
	failed |= 0;
	return failed;
}

int variant5() {
	int failed = 0;
	remove_file("file5");
	FILE* v6 = fopen("file5", "a");
	failed = 0;
	fputs("[6/72]fclose(FILE* (File name, \"a\"))\n", stdout);
	errno = 0;
	int v1137 = fclose(v6);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1137, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf2769260, 0xffffffff, 0, 0, 0xffffffff, "", v6, __LINE__);
	failed |= 0;
	return failed;
}

int variant6() {
	int failed = 0;
	create_empty_file("empty5");
	FILE* v7 = fopen("empty5", "a");
	failed = 0;
	fputs("[7/72]fclose(FILE* (Empty file, \"a\"))\n", stdout);
	errno = 0;
	int v1138 = fclose(v7);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1138, __LINE__);
	failed |= validate_file_pointer((FILE*)0xda01d260, 0xffffffff, 0, 0, 0xffffffff, "", v7, __LINE__);
	failed |= 0;
	return failed;
}

int variant7() {
	int failed = 0;
	create_random_file("random5");
	FILE* v8 = fopen("random5", "a");
	failed = 0;
	fputs("[8/72]fclose(FILE* (File with random text, \"a\"))\n", stdout);
	errno = 0;
	int v1139 = fclose(v8);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1139, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8c605260, 0xffffffff, 0, 0, 0xffffffff, "", v8, __LINE__);
	failed |= 0;
	return failed;
}

int variant8() {
	int failed = 0;
	create_empty_file("empty6");
	FILE* v10 = fopen("empty6", "r+");
	failed = 0;
	fputs("[9/72]fclose(FILE* (Empty file, \"r+\"))\n", stdout);
	errno = 0;
	int v1141 = fclose(v10);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1141, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb2dac260, 0xffffffff, 0, 0, 0xffffffff, "", v10, __LINE__);
	failed |= 0;
	return failed;
}

int variant9() {
	int failed = 0;
	create_random_file("random6");
	FILE* v11 = fopen("random6", "r+");
	failed = 0;
	fputs("[10/72]fclose(FILE* (File with random text, \"r+\"))\n", stdout);
	errno = 0;
	int v1142 = fclose(v11);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1142, __LINE__);
	failed |= validate_file_pointer((FILE*)0x153d7260, 0xffffffff, 0, 0, 0xffffffff, "", v11, __LINE__);
	failed |= 0;
	return failed;
}

int variant10() {
	int failed = 0;
	remove_file("file7");
	FILE* v12 = fopen("file7", "w+");
	failed = 0;
	fputs("[11/72]fclose(FILE* (File name, \"w+\"))\n", stdout);
	errno = 0;
	int v1143 = fclose(v12);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1143, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb6b0e260, 0xffffffff, 0, 0, 0xffffffff, "", v12, __LINE__);
	failed |= 0;
	return failed;
}

int variant11() {
	int failed = 0;
	create_empty_file("empty7");
	FILE* v13 = fopen("empty7", "w+");
	failed = 0;
	fputs("[12/72]fclose(FILE* (Empty file, \"w+\"))\n", stdout);
	errno = 0;
	int v1144 = fclose(v13);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1144, __LINE__);
	failed |= validate_file_pointer((FILE*)0x66c52260, 0xffffffff, 0, 0, 0xffffffff, "", v13, __LINE__);
	failed |= 0;
	return failed;
}

int variant12() {
	int failed = 0;
	create_random_file("random7");
	FILE* v14 = fopen("random7", "w+");
	failed = 0;
	fputs("[13/72]fclose(FILE* (File with random text, \"w+\"))\n", stdout);
	errno = 0;
	int v1145 = fclose(v14);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1145, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6eff6260, 0xffffffff, 0, 0, 0xffffffff, "", v14, __LINE__);
	failed |= 0;
	return failed;
}

int variant13() {
	int failed = 0;
	remove_file("file8");
	FILE* v15 = fopen("file8", "a+");
	failed = 0;
	fputs("[14/72]fclose(FILE* (File name, \"a+\"))\n", stdout);
	errno = 0;
	int v1146 = fclose(v15);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1146, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5833f260, 0xffffffff, 0, 0, 0xffffffff, "", v15, __LINE__);
	failed |= 0;
	return failed;
}

int variant14() {
	int failed = 0;
	create_empty_file("empty8");
	FILE* v16 = fopen("empty8", "a+");
	failed = 0;
	fputs("[15/72]fclose(FILE* (Empty file, \"a+\"))\n", stdout);
	errno = 0;
	int v1147 = fclose(v16);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1147, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbb44e260, 0xffffffff, 0, 0, 0xffffffff, "", v16, __LINE__);
	failed |= 0;
	return failed;
}

int variant15() {
	int failed = 0;
	create_random_file("random8");
	FILE* v17 = fopen("random8", "a+");
	failed = 0;
	fputs("[16/72]fclose(FILE* (File with random text, \"a+\"))\n", stdout);
	errno = 0;
	int v1148 = fclose(v17);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1148, __LINE__);
	failed |= validate_file_pointer((FILE*)0x392b9260, 0xffffffff, 0, 0, 0xffffffff, "", v17, __LINE__);
	failed |= 0;
	return failed;
}

int variant16() {
	int failed = 0;
	create_empty_file("empty9");
	FILE* v19 = fopen("empty9", "rt");
	failed = 0;
	fputs("[17/72]fclose(FILE* (Empty file, \"rt\"))\n", stdout);
	errno = 0;
	int v1150 = fclose(v19);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1150, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2b6ad260, 0xffffffff, 0, 0, 0xffffffff, "", v19, __LINE__);
	failed |= 0;
	return failed;
}

int variant17() {
	int failed = 0;
	create_random_file("random9");
	FILE* v20 = fopen("random9", "rt");
	failed = 0;
	fputs("[18/72]fclose(FILE* (File with random text, \"rt\"))\n", stdout);
	errno = 0;
	int v1151 = fclose(v20);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1151, __LINE__);
	failed |= validate_file_pointer((FILE*)0xba489260, 0xffffffff, 0, 0, 0xffffffff, "", v20, __LINE__);
	failed |= 0;
	return failed;
}

int variant18() {
	int failed = 0;
	remove_file("file10");
	FILE* v21 = fopen("file10", "wt");
	failed = 0;
	fputs("[19/72]fclose(FILE* (File name, \"wt\"))\n", stdout);
	errno = 0;
	int v1152 = fclose(v21);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1152, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc4b2d260, 0xffffffff, 0, 0, 0xffffffff, "", v21, __LINE__);
	failed |= 0;
	return failed;
}

int variant19() {
	int failed = 0;
	create_empty_file("empty10");
	FILE* v22 = fopen("empty10", "wt");
	failed = 0;
	fputs("[20/72]fclose(FILE* (Empty file, \"wt\"))\n", stdout);
	errno = 0;
	int v1153 = fclose(v22);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1153, __LINE__);
	failed |= validate_file_pointer((FILE*)0x980a2260, 0xffffffff, 0, 0, 0xffffffff, "", v22, __LINE__);
	failed |= 0;
	return failed;
}

int variant20() {
	int failed = 0;
	create_random_file("random10");
	FILE* v23 = fopen("random10", "wt");
	failed = 0;
	fputs("[21/72]fclose(FILE* (File with random text, \"wt\"))\n", stdout);
	errno = 0;
	int v1154 = fclose(v23);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1154, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc1d77260, 0xffffffff, 0, 0, 0xffffffff, "", v23, __LINE__);
	failed |= 0;
	return failed;
}

int variant21() {
	int failed = 0;
	remove_file("file11");
	FILE* v24 = fopen("file11", "at");
	failed = 0;
	fputs("[22/72]fclose(FILE* (File name, \"at\"))\n", stdout);
	errno = 0;
	int v1155 = fclose(v24);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1155, __LINE__);
	failed |= validate_file_pointer((FILE*)0x58bd8260, 0xffffffff, 0, 0, 0xffffffff, "", v24, __LINE__);
	failed |= 0;
	return failed;
}

int variant22() {
	int failed = 0;
	create_empty_file("empty11");
	FILE* v25 = fopen("empty11", "at");
	failed = 0;
	fputs("[23/72]fclose(FILE* (Empty file, \"at\"))\n", stdout);
	errno = 0;
	int v1156 = fclose(v25);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1156, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc1d27260, 0xffffffff, 0, 0, 0xffffffff, "", v25, __LINE__);
	failed |= 0;
	return failed;
}

int variant23() {
	int failed = 0;
	create_random_file("random11");
	FILE* v26 = fopen("random11", "at");
	failed = 0;
	fputs("[24/72]fclose(FILE* (File with random text, \"at\"))\n", stdout);
	errno = 0;
	int v1157 = fclose(v26);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1157, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa1311260, 0xffffffff, 0, 0, 0xffffffff, "", v26, __LINE__);
	failed |= 0;
	return failed;
}

int variant24() {
	int failed = 0;
	create_empty_file("empty12");
	FILE* v28 = fopen("empty12", "r+t");
	failed = 0;
	fputs("[25/72]fclose(FILE* (Empty file, \"r+t\"))\n", stdout);
	errno = 0;
	int v1159 = fclose(v28);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1159, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6b2ab260, 0xffffffff, 0, 0, 0xffffffff, "", v28, __LINE__);
	failed |= 0;
	return failed;
}

int variant25() {
	int failed = 0;
	create_random_file("random12");
	FILE* v29 = fopen("random12", "r+t");
	failed = 0;
	fputs("[26/72]fclose(FILE* (File with random text, \"r+t\"))\n", stdout);
	errno = 0;
	int v1160 = fclose(v29);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1160, __LINE__);
	failed |= validate_file_pointer((FILE*)0x61dbf260, 0xffffffff, 0, 0, 0xffffffff, "", v29, __LINE__);
	failed |= 0;
	return failed;
}

int variant26() {
	int failed = 0;
	remove_file("file13");
	FILE* v30 = fopen("file13", "w+t");
	failed = 0;
	fputs("[27/72]fclose(FILE* (File name, \"w+t\"))\n", stdout);
	errno = 0;
	int v1161 = fclose(v30);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1161, __LINE__);
	failed |= validate_file_pointer((FILE*)0x85627260, 0xffffffff, 0, 0, 0xffffffff, "", v30, __LINE__);
	failed |= 0;
	return failed;
}

int variant27() {
	int failed = 0;
	create_empty_file("empty13");
	FILE* v31 = fopen("empty13", "w+t");
	failed = 0;
	fputs("[28/72]fclose(FILE* (Empty file, \"w+t\"))\n", stdout);
	errno = 0;
	int v1162 = fclose(v31);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1162, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3579a260, 0xffffffff, 0, 0, 0xffffffff, "", v31, __LINE__);
	failed |= 0;
	return failed;
}

int variant28() {
	int failed = 0;
	create_random_file("random13");
	FILE* v32 = fopen("random13", "w+t");
	failed = 0;
	fputs("[29/72]fclose(FILE* (File with random text, \"w+t\"))\n", stdout);
	errno = 0;
	int v1163 = fclose(v32);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1163, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2ebd8260, 0xffffffff, 0, 0, 0xffffffff, "", v32, __LINE__);
	failed |= 0;
	return failed;
}

int variant29() {
	int failed = 0;
	remove_file("file14");
	FILE* v33 = fopen("file14", "a+t");
	failed = 0;
	fputs("[30/72]fclose(FILE* (File name, \"a+t\"))\n", stdout);
	errno = 0;
	int v1164 = fclose(v33);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1164, __LINE__);
	failed |= validate_file_pointer((FILE*)0x66ff4260, 0xffffffff, 0, 0, 0xffffffff, "", v33, __LINE__);
	failed |= 0;
	return failed;
}

int variant30() {
	int failed = 0;
	create_empty_file("empty14");
	FILE* v34 = fopen("empty14", "a+t");
	failed = 0;
	fputs("[31/72]fclose(FILE* (Empty file, \"a+t\"))\n", stdout);
	errno = 0;
	int v1165 = fclose(v34);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1165, __LINE__);
	failed |= validate_file_pointer((FILE*)0x49068260, 0xffffffff, 0, 0, 0xffffffff, "", v34, __LINE__);
	failed |= 0;
	return failed;
}

int variant31() {
	int failed = 0;
	create_random_file("random14");
	FILE* v35 = fopen("random14", "a+t");
	failed = 0;
	fputs("[32/72]fclose(FILE* (File with random text, \"a+t\"))\n", stdout);
	errno = 0;
	int v1166 = fclose(v35);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1166, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb6552260, 0xffffffff, 0, 0, 0xffffffff, "", v35, __LINE__);
	failed |= 0;
	return failed;
}

int variant32() {
	int failed = 0;
	create_empty_file("empty15");
	FILE* v37 = fopen("empty15", "rb");
	failed = 0;
	fputs("[33/72]fclose(FILE* (Empty file, \"rb\"))\n", stdout);
	errno = 0;
	int v1168 = fclose(v37);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1168, __LINE__);
	failed |= validate_file_pointer((FILE*)0x83c0f260, 0xffffffff, 0, 0, 0xffffffff, "", v37, __LINE__);
	failed |= 0;
	return failed;
}

int variant33() {
	int failed = 0;
	create_random_file("random15");
	FILE* v38 = fopen("random15", "rb");
	failed = 0;
	fputs("[34/72]fclose(FILE* (File with random text, \"rb\"))\n", stdout);
	errno = 0;
	int v1169 = fclose(v38);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1169, __LINE__);
	failed |= validate_file_pointer((FILE*)0xee740260, 0xffffffff, 0, 0, 0xffffffff, "", v38, __LINE__);
	failed |= 0;
	return failed;
}

int variant34() {
	int failed = 0;
	remove_file("file16");
	FILE* v39 = fopen("file16", "wb");
	failed = 0;
	fputs("[35/72]fclose(FILE* (File name, \"wb\"))\n", stdout);
	errno = 0;
	int v1170 = fclose(v39);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1170, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0a2b6260, 0xffffffff, 0, 0, 0xffffffff, "", v39, __LINE__);
	failed |= 0;
	return failed;
}

int variant35() {
	int failed = 0;
	create_empty_file("empty16");
	FILE* v40 = fopen("empty16", "wb");
	failed = 0;
	fputs("[36/72]fclose(FILE* (Empty file, \"wb\"))\n", stdout);
	errno = 0;
	int v1171 = fclose(v40);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1171, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1945a260, 0xffffffff, 0, 0, 0xffffffff, "", v40, __LINE__);
	failed |= 0;
	return failed;
}

int variant36() {
	int failed = 0;
	create_random_file("random16");
	FILE* v41 = fopen("random16", "wb");
	failed = 0;
	fputs("[37/72]fclose(FILE* (File with random text, \"wb\"))\n", stdout);
	errno = 0;
	int v1172 = fclose(v41);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1172, __LINE__);
	failed |= validate_file_pointer((FILE*)0x55e0e260, 0xffffffff, 0, 0, 0xffffffff, "", v41, __LINE__);
	failed |= 0;
	return failed;
}

int variant37() {
	int failed = 0;
	remove_file("file17");
	FILE* v42 = fopen("file17", "ab");
	failed = 0;
	fputs("[38/72]fclose(FILE* (File name, \"ab\"))\n", stdout);
	errno = 0;
	int v1173 = fclose(v42);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1173, __LINE__);
	failed |= validate_file_pointer((FILE*)0x15c97260, 0xffffffff, 0, 0, 0xffffffff, "", v42, __LINE__);
	failed |= 0;
	return failed;
}

int variant38() {
	int failed = 0;
	create_empty_file("empty17");
	FILE* v43 = fopen("empty17", "ab");
	failed = 0;
	fputs("[39/72]fclose(FILE* (Empty file, \"ab\"))\n", stdout);
	errno = 0;
	int v1174 = fclose(v43);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1174, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1a6ff260, 0xffffffff, 0, 0, 0xffffffff, "", v43, __LINE__);
	failed |= 0;
	return failed;
}

int variant39() {
	int failed = 0;
	create_random_file("random17");
	FILE* v44 = fopen("random17", "ab");
	failed = 0;
	fputs("[40/72]fclose(FILE* (File with random text, \"ab\"))\n", stdout);
	errno = 0;
	int v1175 = fclose(v44);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1175, __LINE__);
	failed |= validate_file_pointer((FILE*)0xddc92260, 0xffffffff, 0, 0, 0xffffffff, "", v44, __LINE__);
	failed |= 0;
	return failed;
}

int variant40() {
	int failed = 0;
	create_empty_file("empty18");
	FILE* v46 = fopen("empty18", "r+b");
	failed = 0;
	fputs("[41/72]fclose(FILE* (Empty file, \"r+b\"))\n", stdout);
	errno = 0;
	int v1177 = fclose(v46);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1177, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8036e260, 0xffffffff, 0, 0, 0xffffffff, "", v46, __LINE__);
	failed |= 0;
	return failed;
}

int variant41() {
	int failed = 0;
	create_random_file("random18");
	FILE* v47 = fopen("random18", "r+b");
	failed = 0;
	fputs("[42/72]fclose(FILE* (File with random text, \"r+b\"))\n", stdout);
	errno = 0;
	int v1178 = fclose(v47);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1178, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2b605260, 0xffffffff, 0, 0, 0xffffffff, "", v47, __LINE__);
	failed |= 0;
	return failed;
}

int variant42() {
	int failed = 0;
	remove_file("file19");
	FILE* v48 = fopen("file19", "w+b");
	failed = 0;
	fputs("[43/72]fclose(FILE* (File name, \"w+b\"))\n", stdout);
	errno = 0;
	int v1179 = fclose(v48);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1179, __LINE__);
	failed |= validate_file_pointer((FILE*)0xee0e5260, 0xffffffff, 0, 0, 0xffffffff, "", v48, __LINE__);
	failed |= 0;
	return failed;
}

int variant43() {
	int failed = 0;
	create_empty_file("empty19");
	FILE* v49 = fopen("empty19", "w+b");
	failed = 0;
	fputs("[44/72]fclose(FILE* (Empty file, \"w+b\"))\n", stdout);
	errno = 0;
	int v1180 = fclose(v49);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1180, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa1ff1260, 0xffffffff, 0, 0, 0xffffffff, "", v49, __LINE__);
	failed |= 0;
	return failed;
}

int variant44() {
	int failed = 0;
	create_random_file("random19");
	FILE* v50 = fopen("random19", "w+b");
	failed = 0;
	fputs("[45/72]fclose(FILE* (File with random text, \"w+b\"))\n", stdout);
	errno = 0;
	int v1181 = fclose(v50);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1181, __LINE__);
	failed |= validate_file_pointer((FILE*)0x07285260, 0xffffffff, 0, 0, 0xffffffff, "", v50, __LINE__);
	failed |= 0;
	return failed;
}

int variant45() {
	int failed = 0;
	remove_file("file20");
	FILE* v51 = fopen("file20", "a+b");
	failed = 0;
	fputs("[46/72]fclose(FILE* (File name, \"a+b\"))\n", stdout);
	errno = 0;
	int v1182 = fclose(v51);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1182, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb589e260, 0xffffffff, 0, 0, 0xffffffff, "", v51, __LINE__);
	failed |= 0;
	return failed;
}

int variant46() {
	int failed = 0;
	create_empty_file("empty20");
	FILE* v52 = fopen("empty20", "a+b");
	failed = 0;
	fputs("[47/72]fclose(FILE* (Empty file, \"a+b\"))\n", stdout);
	errno = 0;
	int v1183 = fclose(v52);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1183, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7b294260, 0xffffffff, 0, 0, 0xffffffff, "", v52, __LINE__);
	failed |= 0;
	return failed;
}

int variant47() {
	int failed = 0;
	create_random_file("random20");
	FILE* v53 = fopen("random20", "a+b");
	failed = 0;
	fputs("[48/72]fclose(FILE* (File with random text, \"a+b\"))\n", stdout);
	errno = 0;
	int v1184 = fclose(v53);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1184, __LINE__);
	failed |= validate_file_pointer((FILE*)0xeaff1260, 0xffffffff, 0, 0, 0xffffffff, "", v53, __LINE__);
	failed |= 0;
	return failed;
}

int variant48() {
	int failed = 0;
	create_empty_file("empty21");
	FILE* v55 = fopen("empty21", "rx");
	failed = 0;
	fputs("[49/72]fclose(FILE* (Empty file, \"rx\"))\n", stdout);
	errno = 0;
	int v1186 = fclose(v55);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1186, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8e564260, 0xffffffff, 0, 0, 0xffffffff, "", v55, __LINE__);
	failed |= 0;
	return failed;
}

int variant49() {
	int failed = 0;
	create_random_file("random21");
	FILE* v56 = fopen("random21", "rx");
	failed = 0;
	fputs("[50/72]fclose(FILE* (File with random text, \"rx\"))\n", stdout);
	errno = 0;
	int v1187 = fclose(v56);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1187, __LINE__);
	failed |= validate_file_pointer((FILE*)0x39f3e260, 0xffffffff, 0, 0, 0xffffffff, "", v56, __LINE__);
	failed |= 0;
	return failed;
}

int variant50() {
	int failed = 0;
	remove_file("file22");
	FILE* v57 = fopen("file22", "wx");
	failed = 0;
	fputs("[51/72]fclose(FILE* (File name, \"wx\"))\n", stdout);
	errno = 0;
	int v1188 = fclose(v57);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1188, __LINE__);
	failed |= validate_file_pointer((FILE*)0x50222260, 0xffffffff, 0, 0, 0xffffffff, "", v57, __LINE__);
	failed |= 0;
	return failed;
}

int variant51() {
	int failed = 0;
	remove_file("file23");
	FILE* v60 = fopen("file23", "ax");
	failed = 0;
	fputs("[52/72]fclose(FILE* (File name, \"ax\"))\n", stdout);
	errno = 0;
	int v1191 = fclose(v60);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1191, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1b0ec260, 0xffffffff, 0, 0, 0xffffffff, "", v60, __LINE__);
	failed |= 0;
	return failed;
}

int variant52() {
	int failed = 0;
	create_empty_file("empty24");
	FILE* v64 = fopen("empty24", "r+x");
	failed = 0;
	fputs("[53/72]fclose(FILE* (Empty file, \"r+x\"))\n", stdout);
	errno = 0;
	int v1195 = fclose(v64);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1195, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfd88f260, 0xffffffff, 0, 0, 0xffffffff, "", v64, __LINE__);
	failed |= 0;
	return failed;
}

int variant53() {
	int failed = 0;
	create_random_file("random24");
	FILE* v65 = fopen("random24", "r+x");
	failed = 0;
	fputs("[54/72]fclose(FILE* (File with random text, \"r+x\"))\n", stdout);
	errno = 0;
	int v1196 = fclose(v65);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1196, __LINE__);
	failed |= validate_file_pointer((FILE*)0xded5b260, 0xffffffff, 0, 0, 0xffffffff, "", v65, __LINE__);
	failed |= 0;
	return failed;
}

int variant54() {
	int failed = 0;
	remove_file("file25");
	FILE* v66 = fopen("file25", "w+x");
	failed = 0;
	fputs("[55/72]fclose(FILE* (File name, \"w+x\"))\n", stdout);
	errno = 0;
	int v1197 = fclose(v66);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1197, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1c571260, 0xffffffff, 0, 0, 0xffffffff, "", v66, __LINE__);
	failed |= 0;
	return failed;
}

int variant55() {
	int failed = 0;
	remove_file("file26");
	FILE* v69 = fopen("file26", "a+x");
	failed = 0;
	fputs("[56/72]fclose(FILE* (File name, \"a+x\"))\n", stdout);
	errno = 0;
	int v1200 = fclose(v69);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1200, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdf6b6260, 0xffffffff, 0, 0, 0xffffffff, "", v69, __LINE__);
	failed |= 0;
	return failed;
}

int variant56() {
	int failed = 0;
	create_empty_file("empty27");
	FILE* v73 = fopen("empty27", "rtx");
	failed = 0;
	fputs("[57/72]fclose(FILE* (Empty file, \"rtx\"))\n", stdout);
	errno = 0;
	int v1204 = fclose(v73);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1204, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd2d22260, 0xffffffff, 0, 0, 0xffffffff, "", v73, __LINE__);
	failed |= 0;
	return failed;
}

int variant57() {
	int failed = 0;
	create_random_file("random27");
	FILE* v74 = fopen("random27", "rtx");
	failed = 0;
	fputs("[58/72]fclose(FILE* (File with random text, \"rtx\"))\n", stdout);
	errno = 0;
	int v1205 = fclose(v74);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1205, __LINE__);
	failed |= validate_file_pointer((FILE*)0x74118260, 0xffffffff, 0, 0, 0xffffffff, "", v74, __LINE__);
	failed |= 0;
	return failed;
}

int variant58() {
	int failed = 0;
	remove_file("file28");
	FILE* v75 = fopen("file28", "wtx");
	failed = 0;
	fputs("[59/72]fclose(FILE* (File name, \"wtx\"))\n", stdout);
	errno = 0;
	int v1206 = fclose(v75);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1206, __LINE__);
	failed |= validate_file_pointer((FILE*)0x618a5260, 0xffffffff, 0, 0, 0xffffffff, "", v75, __LINE__);
	failed |= 0;
	return failed;
}

int variant59() {
	int failed = 0;
	remove_file("file29");
	FILE* v78 = fopen("file29", "atx");
	failed = 0;
	fputs("[60/72]fclose(FILE* (File name, \"atx\"))\n", stdout);
	errno = 0;
	int v1209 = fclose(v78);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1209, __LINE__);
	failed |= validate_file_pointer((FILE*)0x94133260, 0xffffffff, 0, 0, 0xffffffff, "", v78, __LINE__);
	failed |= 0;
	return failed;
}

int variant60() {
	int failed = 0;
	create_empty_file("empty30");
	FILE* v82 = fopen("empty30", "r+tx");
	failed = 0;
	fputs("[61/72]fclose(FILE* (Empty file, \"r+tx\"))\n", stdout);
	errno = 0;
	int v1213 = fclose(v82);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1213, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe05f7260, 0xffffffff, 0, 0, 0xffffffff, "", v82, __LINE__);
	failed |= 0;
	return failed;
}

int variant61() {
	int failed = 0;
	create_random_file("random30");
	FILE* v83 = fopen("random30", "r+tx");
	failed = 0;
	fputs("[62/72]fclose(FILE* (File with random text, \"r+tx\"))\n", stdout);
	errno = 0;
	int v1214 = fclose(v83);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1214, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6a1f2260, 0xffffffff, 0, 0, 0xffffffff, "", v83, __LINE__);
	failed |= 0;
	return failed;
}

int variant62() {
	int failed = 0;
	remove_file("file31");
	FILE* v84 = fopen("file31", "w+tx");
	failed = 0;
	fputs("[63/72]fclose(FILE* (File name, \"w+tx\"))\n", stdout);
	errno = 0;
	int v1215 = fclose(v84);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1215, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc4e2c260, 0xffffffff, 0, 0, 0xffffffff, "", v84, __LINE__);
	failed |= 0;
	return failed;
}

int variant63() {
	int failed = 0;
	remove_file("file32");
	FILE* v87 = fopen("file32", "a+tx");
	failed = 0;
	fputs("[64/72]fclose(FILE* (File name, \"a+tx\"))\n", stdout);
	errno = 0;
	int v1218 = fclose(v87);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1218, __LINE__);
	failed |= validate_file_pointer((FILE*)0x35cf3260, 0xffffffff, 0, 0, 0xffffffff, "", v87, __LINE__);
	failed |= 0;
	return failed;
}

int variant64() {
	int failed = 0;
	create_empty_file("empty33");
	FILE* v91 = fopen("empty33", "rbx");
	failed = 0;
	fputs("[65/72]fclose(FILE* (Empty file, \"rbx\"))\n", stdout);
	errno = 0;
	int v1222 = fclose(v91);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1222, __LINE__);
	failed |= validate_file_pointer((FILE*)0x664db260, 0xffffffff, 0, 0, 0xffffffff, "", v91, __LINE__);
	failed |= 0;
	return failed;
}

int variant65() {
	int failed = 0;
	create_random_file("random33");
	FILE* v92 = fopen("random33", "rbx");
	failed = 0;
	fputs("[66/72]fclose(FILE* (File with random text, \"rbx\"))\n", stdout);
	errno = 0;
	int v1223 = fclose(v92);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1223, __LINE__);
	failed |= validate_file_pointer((FILE*)0x83a41260, 0xffffffff, 0, 0, 0xffffffff, "", v92, __LINE__);
	failed |= 0;
	return failed;
}

int variant66() {
	int failed = 0;
	remove_file("file34");
	FILE* v93 = fopen("file34", "wbx");
	failed = 0;
	fputs("[67/72]fclose(FILE* (File name, \"wbx\"))\n", stdout);
	errno = 0;
	int v1224 = fclose(v93);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1224, __LINE__);
	failed |= validate_file_pointer((FILE*)0x885b5260, 0xffffffff, 0, 0, 0xffffffff, "", v93, __LINE__);
	failed |= 0;
	return failed;
}

int variant67() {
	int failed = 0;
	remove_file("file35");
	FILE* v96 = fopen("file35", "abx");
	failed = 0;
	fputs("[68/72]fclose(FILE* (File name, \"abx\"))\n", stdout);
	errno = 0;
	int v1227 = fclose(v96);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1227, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3f52d260, 0xffffffff, 0, 0, 0xffffffff, "", v96, __LINE__);
	failed |= 0;
	return failed;
}

int variant68() {
	int failed = 0;
	create_empty_file("empty36");
	FILE* v100 = fopen("empty36", "r+bx");
	failed = 0;
	fputs("[69/72]fclose(FILE* (Empty file, \"r+bx\"))\n", stdout);
	errno = 0;
	int v1231 = fclose(v100);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1231, __LINE__);
	failed |= validate_file_pointer((FILE*)0xabff5260, 0xffffffff, 0, 0, 0xffffffff, "", v100, __LINE__);
	failed |= 0;
	return failed;
}

int variant69() {
	int failed = 0;
	create_random_file("random36");
	FILE* v101 = fopen("random36", "r+bx");
	failed = 0;
	fputs("[70/72]fclose(FILE* (File with random text, \"r+bx\"))\n", stdout);
	errno = 0;
	int v1232 = fclose(v101);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1232, __LINE__);
	failed |= validate_file_pointer((FILE*)0x44cac260, 0xffffffff, 0, 0, 0xffffffff, "", v101, __LINE__);
	failed |= 0;
	return failed;
}

int variant70() {
	int failed = 0;
	remove_file("file37");
	FILE* v102 = fopen("file37", "w+bx");
	failed = 0;
	fputs("[71/72]fclose(FILE* (File name, \"w+bx\"))\n", stdout);
	errno = 0;
	int v1233 = fclose(v102);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1233, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5028c260, 0xffffffff, 0, 0, 0xffffffff, "", v102, __LINE__);
	failed |= 0;
	return failed;
}

int variant71() {
	int failed = 0;
	remove_file("file38");
	FILE* v105 = fopen("file38", "a+bx");
	failed = 0;
	fputs("[72/72]fclose(FILE* (File name, \"a+bx\"))\n", stdout);
	errno = 0;
	int v1236 = fclose(v105);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_int(0, v1236, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5b14e260, 0xffffffff, 0, 0, 0xffffffff, "", v105, __LINE__);
	failed |= 0;
	return failed;
}

int segfault0() {
	int exitcode = 0;
	if (fork() == 0) {
		int v1130 = fclose(NULL);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[1/37]fclose(NULL)\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault1() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file3");
		FILE* v0 = fopen("file3", "r");
		int v1131 = fclose(v0);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[2/37]fclose(FILE* (File name, \"r\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault2() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file6");
		FILE* v9 = fopen("file6", "r+");
		int v1140 = fclose(v9);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[3/37]fclose(FILE* (File name, \"r+\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault3() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file9");
		FILE* v18 = fopen("file9", "rt");
		int v1149 = fclose(v18);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[4/37]fclose(FILE* (File name, \"rt\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault4() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file12");
		FILE* v27 = fopen("file12", "r+t");
		int v1158 = fclose(v27);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[5/37]fclose(FILE* (File name, \"r+t\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault5() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file15");
		FILE* v36 = fopen("file15", "rb");
		int v1167 = fclose(v36);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[6/37]fclose(FILE* (File name, \"rb\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault6() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file18");
		FILE* v45 = fopen("file18", "r+b");
		int v1176 = fclose(v45);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[7/37]fclose(FILE* (File name, \"r+b\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault7() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file21");
		FILE* v54 = fopen("file21", "rx");
		int v1185 = fclose(v54);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[8/37]fclose(FILE* (File name, \"rx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault8() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty22");
		FILE* v58 = fopen("empty22", "wx");
		int v1189 = fclose(v58);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[9/37]fclose(FILE* (Empty file, \"wx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault9() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random22");
		FILE* v59 = fopen("random22", "wx");
		int v1190 = fclose(v59);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[10/37]fclose(FILE* (File with random text, \"wx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault10() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty23");
		FILE* v61 = fopen("empty23", "ax");
		int v1192 = fclose(v61);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[11/37]fclose(FILE* (Empty file, \"ax\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault11() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random23");
		FILE* v62 = fopen("random23", "ax");
		int v1193 = fclose(v62);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[12/37]fclose(FILE* (File with random text, \"ax\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault12() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file24");
		FILE* v63 = fopen("file24", "r+x");
		int v1194 = fclose(v63);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[13/37]fclose(FILE* (File name, \"r+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault13() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty25");
		FILE* v67 = fopen("empty25", "w+x");
		int v1198 = fclose(v67);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[14/37]fclose(FILE* (Empty file, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault14() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random25");
		FILE* v68 = fopen("random25", "w+x");
		int v1199 = fclose(v68);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[15/37]fclose(FILE* (File with random text, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault15() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty26");
		FILE* v70 = fopen("empty26", "a+x");
		int v1201 = fclose(v70);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[16/37]fclose(FILE* (Empty file, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault16() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random26");
		FILE* v71 = fopen("random26", "a+x");
		int v1202 = fclose(v71);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[17/37]fclose(FILE* (File with random text, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault17() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file27");
		FILE* v72 = fopen("file27", "rtx");
		int v1203 = fclose(v72);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[18/37]fclose(FILE* (File name, \"rtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault18() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty28");
		FILE* v76 = fopen("empty28", "wtx");
		int v1207 = fclose(v76);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[19/37]fclose(FILE* (Empty file, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault19() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random28");
		FILE* v77 = fopen("random28", "wtx");
		int v1208 = fclose(v77);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[20/37]fclose(FILE* (File with random text, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault20() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty29");
		FILE* v79 = fopen("empty29", "atx");
		int v1210 = fclose(v79);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[21/37]fclose(FILE* (Empty file, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault21() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random29");
		FILE* v80 = fopen("random29", "atx");
		int v1211 = fclose(v80);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[22/37]fclose(FILE* (File with random text, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault22() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file30");
		FILE* v81 = fopen("file30", "r+tx");
		int v1212 = fclose(v81);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[23/37]fclose(FILE* (File name, \"r+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault23() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty31");
		FILE* v85 = fopen("empty31", "w+tx");
		int v1216 = fclose(v85);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[24/37]fclose(FILE* (Empty file, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault24() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random31");
		FILE* v86 = fopen("random31", "w+tx");
		int v1217 = fclose(v86);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[25/37]fclose(FILE* (File with random text, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault25() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty32");
		FILE* v88 = fopen("empty32", "a+tx");
		int v1219 = fclose(v88);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[26/37]fclose(FILE* (Empty file, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault26() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random32");
		FILE* v89 = fopen("random32", "a+tx");
		int v1220 = fclose(v89);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[27/37]fclose(FILE* (File with random text, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault27() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file33");
		FILE* v90 = fopen("file33", "rbx");
		int v1221 = fclose(v90);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[28/37]fclose(FILE* (File name, \"rbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault28() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty34");
		FILE* v94 = fopen("empty34", "wbx");
		int v1225 = fclose(v94);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[29/37]fclose(FILE* (Empty file, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault29() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random34");
		FILE* v95 = fopen("random34", "wbx");
		int v1226 = fclose(v95);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[30/37]fclose(FILE* (File with random text, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault30() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty35");
		FILE* v97 = fopen("empty35", "abx");
		int v1228 = fclose(v97);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[31/37]fclose(FILE* (Empty file, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault31() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random35");
		FILE* v98 = fopen("random35", "abx");
		int v1229 = fclose(v98);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[32/37]fclose(FILE* (File with random text, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault32() {
	int exitcode = 0;
	if (fork() == 0) {
		remove_file("file36");
		FILE* v99 = fopen("file36", "r+bx");
		int v1230 = fclose(v99);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[33/37]fclose(FILE* (File name, \"r+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault33() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty37");
		FILE* v103 = fopen("empty37", "w+bx");
		int v1234 = fclose(v103);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[34/37]fclose(FILE* (Empty file, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault34() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random37");
		FILE* v104 = fopen("random37", "w+bx");
		int v1235 = fclose(v104);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[35/37]fclose(FILE* (File with random text, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault35() {
	int exitcode = 0;
	if (fork() == 0) {
		create_empty_file("empty38");
		FILE* v106 = fopen("empty38", "a+bx");
		int v1237 = fclose(v106);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[36/37]fclose(FILE* (Empty file, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault36() {
	int exitcode = 0;
	if (fork() == 0) {
		create_random_file("random38");
		FILE* v107 = fopen("random38", "a+bx");
		int v1238 = fclose(v107);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[37/37]fclose(FILE* (File with random text, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int main() {
	int error = 0;
	
	error |= variant0();
	error |= variant1();
	error |= variant2();
	error |= variant3();
	error |= variant4();
	error |= variant5();
	error |= variant6();
	error |= variant7();
	error |= variant8();
	error |= variant9();
	error |= variant10();
	error |= variant11();
	error |= variant12();
	error |= variant13();
	error |= variant14();
	error |= variant15();
	error |= variant16();
	error |= variant17();
	error |= variant18();
	error |= variant19();
	error |= variant20();
	error |= variant21();
	error |= variant22();
	error |= variant23();
	error |= variant24();
	error |= variant25();
	error |= variant26();
	error |= variant27();
	error |= variant28();
	error |= variant29();
	error |= variant30();
	error |= variant31();
	error |= variant32();
	error |= variant33();
	error |= variant34();
	error |= variant35();
	error |= variant36();
	error |= variant37();
	error |= variant38();
	error |= variant39();
	error |= variant40();
	error |= variant41();
	error |= variant42();
	error |= variant43();
	error |= variant44();
	error |= variant45();
	error |= variant46();
	error |= variant47();
	error |= variant48();
	error |= variant49();
	error |= variant50();
	error |= variant51();
	error |= variant52();
	error |= variant53();
	error |= variant54();
	error |= variant55();
	error |= variant56();
	error |= variant57();
	error |= variant58();
	error |= variant59();
	error |= variant60();
	error |= variant61();
	error |= variant62();
	error |= variant63();
	error |= variant64();
	error |= variant65();
	error |= variant66();
	error |= variant67();
	error |= variant68();
	error |= variant69();
	error |= variant70();
	error |= variant71();
	error |= segfault0();
	error |= segfault1();
	error |= segfault2();
	error |= segfault3();
	error |= segfault4();
	error |= segfault5();
	error |= segfault6();
	error |= segfault7();
	error |= segfault8();
	error |= segfault9();
	error |= segfault10();
	error |= segfault11();
	error |= segfault12();
	error |= segfault13();
	error |= segfault14();
	error |= segfault15();
	error |= segfault16();
	error |= segfault17();
	error |= segfault18();
	error |= segfault19();
	error |= segfault20();
	error |= segfault21();
	error |= segfault22();
	error |= segfault23();
	error |= segfault24();
	error |= segfault25();
	error |= segfault26();
	error |= segfault27();
	error |= segfault28();
	error |= segfault29();
	error |= segfault30();
	error |= segfault31();
	error |= segfault32();
	error |= segfault33();
	error |= segfault34();
	error |= segfault35();
	error |= segfault36();
	return error;
}

