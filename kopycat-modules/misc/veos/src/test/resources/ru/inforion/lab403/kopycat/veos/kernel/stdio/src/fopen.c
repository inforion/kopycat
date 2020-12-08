#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include "builtins.h"

int variant0() {
	int failed = 0;
	failed = 0;
	fputs("[1/148]fopen(NULL, \"r\")\n", stdout);
	errno = 0;
	FILE* v982 = fopen(NULL, "r");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v982, __LINE__);
	return failed;
}

int variant1() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[2/148]fopen(File name, \"r\")\n", stdout);
	errno = 0;
	FILE* v983 = fopen("file75", "r");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v983, __LINE__);
	return failed;
}

int variant2() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[3/148]fopen(Empty file, \"r\")\n", stdout);
	errno = 0;
	FILE* v984 = fopen("empty75", "r");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x75b39260, 0x00000000, 0, 0, 0x00000000, "", v984, __LINE__);
	return failed;
}

int variant3() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[4/148]fopen(File with random text, \"r\")\n", stdout);
	errno = 0;
	FILE* v985 = fopen("random75", "r");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4e5cf260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v985, __LINE__);
	return failed;
}

int variant4() {
	int failed = 0;
	failed = 0;
	fputs("[5/148]fopen(NULL, \"w\")\n", stdout);
	errno = 0;
	FILE* v986 = fopen(NULL, "w");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v986, __LINE__);
	return failed;
}

int variant5() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[6/148]fopen(File name, \"w\")\n", stdout);
	errno = 0;
	FILE* v987 = fopen("file75", "w");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0bb29260, 0x00000000, 0, 0, 0x00000000, "", v987, __LINE__);
	return failed;
}

int variant6() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[7/148]fopen(Empty file, \"w\")\n", stdout);
	errno = 0;
	FILE* v988 = fopen("empty75", "w");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2f3eb260, 0x00000000, 0, 0, 0x00000000, "", v988, __LINE__);
	return failed;
}

int variant7() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[8/148]fopen(File with random text, \"w\")\n", stdout);
	errno = 0;
	FILE* v989 = fopen("random75", "w");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc4680260, 0x00000000, 0, 0, 0x00000000, "", v989, __LINE__);
	return failed;
}

int variant8() {
	int failed = 0;
	failed = 0;
	fputs("[9/148]fopen(NULL, \"a\")\n", stdout);
	errno = 0;
	FILE* v990 = fopen(NULL, "a");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v990, __LINE__);
	return failed;
}

int variant9() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[10/148]fopen(File name, \"a\")\n", stdout);
	errno = 0;
	FILE* v991 = fopen("file75", "a");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbaed4260, 0x00000000, 0, 0, 0x00000000, "", v991, __LINE__);
	return failed;
}

int variant10() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[11/148]fopen(Empty file, \"a\")\n", stdout);
	errno = 0;
	FILE* v992 = fopen("empty75", "a");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa444b260, 0x00000000, 0, 0, 0x00000000, "", v992, __LINE__);
	return failed;
}

int variant11() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[12/148]fopen(File with random text, \"a\")\n", stdout);
	errno = 0;
	FILE* v993 = fopen("random75", "a");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8c8e2260, 0x00000400, 0, 0, 0x00000400, "", v993, __LINE__);
	return failed;
}

int variant12() {
	int failed = 0;
	failed = 0;
	fputs("[13/148]fopen(NULL, \"r+\")\n", stdout);
	errno = 0;
	FILE* v994 = fopen(NULL, "r+");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v994, __LINE__);
	return failed;
}

int variant13() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[14/148]fopen(File name, \"r+\")\n", stdout);
	errno = 0;
	FILE* v995 = fopen("file75", "r+");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v995, __LINE__);
	return failed;
}

int variant14() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[15/148]fopen(Empty file, \"r+\")\n", stdout);
	errno = 0;
	FILE* v996 = fopen("empty75", "r+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xba22d260, 0x00000000, 0, 0, 0x00000000, "", v996, __LINE__);
	return failed;
}

int variant15() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[16/148]fopen(File with random text, \"r+\")\n", stdout);
	errno = 0;
	FILE* v997 = fopen("random75", "r+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x56380260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v997, __LINE__);
	return failed;
}

int variant16() {
	int failed = 0;
	failed = 0;
	fputs("[17/148]fopen(NULL, \"w+\")\n", stdout);
	errno = 0;
	FILE* v998 = fopen(NULL, "w+");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v998, __LINE__);
	return failed;
}

int variant17() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[18/148]fopen(File name, \"w+\")\n", stdout);
	errno = 0;
	FILE* v999 = fopen("file75", "w+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x09ad9260, 0x00000000, 0, 0, 0x00000000, "", v999, __LINE__);
	return failed;
}

int variant18() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[19/148]fopen(Empty file, \"w+\")\n", stdout);
	errno = 0;
	FILE* v1000 = fopen("empty75", "w+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb1546260, 0x00000000, 0, 0, 0x00000000, "", v1000, __LINE__);
	return failed;
}

int variant19() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[20/148]fopen(File with random text, \"w+\")\n", stdout);
	errno = 0;
	FILE* v1001 = fopen("random75", "w+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd235d260, 0x00000000, 0, 0, 0x00000000, "", v1001, __LINE__);
	return failed;
}

int variant20() {
	int failed = 0;
	failed = 0;
	fputs("[21/148]fopen(NULL, \"a+\")\n", stdout);
	errno = 0;
	FILE* v1002 = fopen(NULL, "a+");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1002, __LINE__);
	return failed;
}

int variant21() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[22/148]fopen(File name, \"a+\")\n", stdout);
	errno = 0;
	FILE* v1003 = fopen("file75", "a+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1072c260, 0x00000000, 0, 0, 0x00000000, "", v1003, __LINE__);
	return failed;
}

int variant22() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[23/148]fopen(Empty file, \"a+\")\n", stdout);
	errno = 0;
	FILE* v1004 = fopen("empty75", "a+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x18380260, 0x00000000, 0, 0, 0x00000000, "", v1004, __LINE__);
	return failed;
}

int variant23() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[24/148]fopen(File with random text, \"a+\")\n", stdout);
	errno = 0;
	FILE* v1005 = fopen("random75", "a+");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x094f0260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1005, __LINE__);
	return failed;
}

int variant24() {
	int failed = 0;
	failed = 0;
	fputs("[25/148]fopen(NULL, \"rt\")\n", stdout);
	errno = 0;
	FILE* v1006 = fopen(NULL, "rt");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1006, __LINE__);
	return failed;
}

int variant25() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[26/148]fopen(File name, \"rt\")\n", stdout);
	errno = 0;
	FILE* v1007 = fopen("file75", "rt");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1007, __LINE__);
	return failed;
}

int variant26() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[27/148]fopen(Empty file, \"rt\")\n", stdout);
	errno = 0;
	FILE* v1008 = fopen("empty75", "rt");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x31e4d260, 0x00000000, 0, 0, 0x00000000, "", v1008, __LINE__);
	return failed;
}

int variant27() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[28/148]fopen(File with random text, \"rt\")\n", stdout);
	errno = 0;
	FILE* v1009 = fopen("random75", "rt");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x97ca2260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1009, __LINE__);
	return failed;
}

int variant28() {
	int failed = 0;
	failed = 0;
	fputs("[29/148]fopen(NULL, \"wt\")\n", stdout);
	errno = 0;
	FILE* v1010 = fopen(NULL, "wt");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1010, __LINE__);
	return failed;
}

int variant29() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[30/148]fopen(File name, \"wt\")\n", stdout);
	errno = 0;
	FILE* v1011 = fopen("file75", "wt");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x641aa260, 0x00000000, 0, 0, 0x00000000, "", v1011, __LINE__);
	return failed;
}

int variant30() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[31/148]fopen(Empty file, \"wt\")\n", stdout);
	errno = 0;
	FILE* v1012 = fopen("empty75", "wt");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xaa428260, 0x00000000, 0, 0, 0x00000000, "", v1012, __LINE__);
	return failed;
}

int variant31() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[32/148]fopen(File with random text, \"wt\")\n", stdout);
	errno = 0;
	FILE* v1013 = fopen("random75", "wt");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa153a260, 0x00000000, 0, 0, 0x00000000, "", v1013, __LINE__);
	return failed;
}

int variant32() {
	int failed = 0;
	failed = 0;
	fputs("[33/148]fopen(NULL, \"at\")\n", stdout);
	errno = 0;
	FILE* v1014 = fopen(NULL, "at");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1014, __LINE__);
	return failed;
}

int variant33() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[34/148]fopen(File name, \"at\")\n", stdout);
	errno = 0;
	FILE* v1015 = fopen("file75", "at");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x94d14260, 0x00000000, 0, 0, 0x00000000, "", v1015, __LINE__);
	return failed;
}

int variant34() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[35/148]fopen(Empty file, \"at\")\n", stdout);
	errno = 0;
	FILE* v1016 = fopen("empty75", "at");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf91cc260, 0x00000000, 0, 0, 0x00000000, "", v1016, __LINE__);
	return failed;
}

int variant35() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[36/148]fopen(File with random text, \"at\")\n", stdout);
	errno = 0;
	FILE* v1017 = fopen("random75", "at");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9d403260, 0x00000400, 0, 0, 0x00000400, "", v1017, __LINE__);
	return failed;
}

int variant36() {
	int failed = 0;
	failed = 0;
	fputs("[37/148]fopen(NULL, \"r+t\")\n", stdout);
	errno = 0;
	FILE* v1018 = fopen(NULL, "r+t");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1018, __LINE__);
	return failed;
}

int variant37() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[38/148]fopen(File name, \"r+t\")\n", stdout);
	errno = 0;
	FILE* v1019 = fopen("file75", "r+t");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1019, __LINE__);
	return failed;
}

int variant38() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[39/148]fopen(Empty file, \"r+t\")\n", stdout);
	errno = 0;
	FILE* v1020 = fopen("empty75", "r+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x700dc260, 0x00000000, 0, 0, 0x00000000, "", v1020, __LINE__);
	return failed;
}

int variant39() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[40/148]fopen(File with random text, \"r+t\")\n", stdout);
	errno = 0;
	FILE* v1021 = fopen("random75", "r+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdf3a0260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1021, __LINE__);
	return failed;
}

int variant40() {
	int failed = 0;
	failed = 0;
	fputs("[41/148]fopen(NULL, \"w+t\")\n", stdout);
	errno = 0;
	FILE* v1022 = fopen(NULL, "w+t");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1022, __LINE__);
	return failed;
}

int variant41() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[42/148]fopen(File name, \"w+t\")\n", stdout);
	errno = 0;
	FILE* v1023 = fopen("file75", "w+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x07c3c260, 0x00000000, 0, 0, 0x00000000, "", v1023, __LINE__);
	return failed;
}

int variant42() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[43/148]fopen(Empty file, \"w+t\")\n", stdout);
	errno = 0;
	FILE* v1024 = fopen("empty75", "w+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7317c260, 0x00000000, 0, 0, 0x00000000, "", v1024, __LINE__);
	return failed;
}

int variant43() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[44/148]fopen(File with random text, \"w+t\")\n", stdout);
	errno = 0;
	FILE* v1025 = fopen("random75", "w+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x116c6260, 0x00000000, 0, 0, 0x00000000, "", v1025, __LINE__);
	return failed;
}

int variant44() {
	int failed = 0;
	failed = 0;
	fputs("[45/148]fopen(NULL, \"a+t\")\n", stdout);
	errno = 0;
	FILE* v1026 = fopen(NULL, "a+t");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1026, __LINE__);
	return failed;
}

int variant45() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[46/148]fopen(File name, \"a+t\")\n", stdout);
	errno = 0;
	FILE* v1027 = fopen("file75", "a+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6ef9a260, 0x00000000, 0, 0, 0x00000000, "", v1027, __LINE__);
	return failed;
}

int variant46() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[47/148]fopen(Empty file, \"a+t\")\n", stdout);
	errno = 0;
	FILE* v1028 = fopen("empty75", "a+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x471e7260, 0x00000000, 0, 0, 0x00000000, "", v1028, __LINE__);
	return failed;
}

int variant47() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[48/148]fopen(File with random text, \"a+t\")\n", stdout);
	errno = 0;
	FILE* v1029 = fopen("random75", "a+t");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1bbb2260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1029, __LINE__);
	return failed;
}

int variant48() {
	int failed = 0;
	failed = 0;
	fputs("[49/148]fopen(NULL, \"rb\")\n", stdout);
	errno = 0;
	FILE* v1030 = fopen(NULL, "rb");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1030, __LINE__);
	return failed;
}

int variant49() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[50/148]fopen(File name, \"rb\")\n", stdout);
	errno = 0;
	FILE* v1031 = fopen("file75", "rb");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1031, __LINE__);
	return failed;
}

int variant50() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[51/148]fopen(Empty file, \"rb\")\n", stdout);
	errno = 0;
	FILE* v1032 = fopen("empty75", "rb");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6f81f260, 0x00000000, 0, 0, 0x00000000, "", v1032, __LINE__);
	return failed;
}

int variant51() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[52/148]fopen(File with random text, \"rb\")\n", stdout);
	errno = 0;
	FILE* v1033 = fopen("random75", "rb");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7fdbd260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1033, __LINE__);
	return failed;
}

int variant52() {
	int failed = 0;
	failed = 0;
	fputs("[53/148]fopen(NULL, \"wb\")\n", stdout);
	errno = 0;
	FILE* v1034 = fopen(NULL, "wb");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1034, __LINE__);
	return failed;
}

int variant53() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[54/148]fopen(File name, \"wb\")\n", stdout);
	errno = 0;
	FILE* v1035 = fopen("file75", "wb");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x577bf260, 0x00000000, 0, 0, 0x00000000, "", v1035, __LINE__);
	return failed;
}

int variant54() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[55/148]fopen(Empty file, \"wb\")\n", stdout);
	errno = 0;
	FILE* v1036 = fopen("empty75", "wb");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x175aa260, 0x00000000, 0, 0, 0x00000000, "", v1036, __LINE__);
	return failed;
}

int variant55() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[56/148]fopen(File with random text, \"wb\")\n", stdout);
	errno = 0;
	FILE* v1037 = fopen("random75", "wb");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xeebb3260, 0x00000000, 0, 0, 0x00000000, "", v1037, __LINE__);
	return failed;
}

int variant56() {
	int failed = 0;
	failed = 0;
	fputs("[57/148]fopen(NULL, \"ab\")\n", stdout);
	errno = 0;
	FILE* v1038 = fopen(NULL, "ab");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1038, __LINE__);
	return failed;
}

int variant57() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[58/148]fopen(File name, \"ab\")\n", stdout);
	errno = 0;
	FILE* v1039 = fopen("file75", "ab");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc245b260, 0x00000000, 0, 0, 0x00000000, "", v1039, __LINE__);
	return failed;
}

int variant58() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[59/148]fopen(Empty file, \"ab\")\n", stdout);
	errno = 0;
	FILE* v1040 = fopen("empty75", "ab");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xeab60260, 0x00000000, 0, 0, 0x00000000, "", v1040, __LINE__);
	return failed;
}

int variant59() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[60/148]fopen(File with random text, \"ab\")\n", stdout);
	errno = 0;
	FILE* v1041 = fopen("random75", "ab");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb9681260, 0x00000400, 0, 0, 0x00000400, "", v1041, __LINE__);
	return failed;
}

int variant60() {
	int failed = 0;
	failed = 0;
	fputs("[61/148]fopen(NULL, \"r+b\")\n", stdout);
	errno = 0;
	FILE* v1042 = fopen(NULL, "r+b");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1042, __LINE__);
	return failed;
}

int variant61() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[62/148]fopen(File name, \"r+b\")\n", stdout);
	errno = 0;
	FILE* v1043 = fopen("file75", "r+b");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1043, __LINE__);
	return failed;
}

int variant62() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[63/148]fopen(Empty file, \"r+b\")\n", stdout);
	errno = 0;
	FILE* v1044 = fopen("empty75", "r+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x781bf260, 0x00000000, 0, 0, 0x00000000, "", v1044, __LINE__);
	return failed;
}

int variant63() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[64/148]fopen(File with random text, \"r+b\")\n", stdout);
	errno = 0;
	FILE* v1045 = fopen("random75", "r+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd00a0260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1045, __LINE__);
	return failed;
}

int variant64() {
	int failed = 0;
	failed = 0;
	fputs("[65/148]fopen(NULL, \"w+b\")\n", stdout);
	errno = 0;
	FILE* v1046 = fopen(NULL, "w+b");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1046, __LINE__);
	return failed;
}

int variant65() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[66/148]fopen(File name, \"w+b\")\n", stdout);
	errno = 0;
	FILE* v1047 = fopen("file75", "w+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x93135260, 0x00000000, 0, 0, 0x00000000, "", v1047, __LINE__);
	return failed;
}

int variant66() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[67/148]fopen(Empty file, \"w+b\")\n", stdout);
	errno = 0;
	FILE* v1048 = fopen("empty75", "w+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x455cf260, 0x00000000, 0, 0, 0x00000000, "", v1048, __LINE__);
	return failed;
}

int variant67() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[68/148]fopen(File with random text, \"w+b\")\n", stdout);
	errno = 0;
	FILE* v1049 = fopen("random75", "w+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5a2f3260, 0x00000000, 0, 0, 0x00000000, "", v1049, __LINE__);
	return failed;
}

int variant68() {
	int failed = 0;
	failed = 0;
	fputs("[69/148]fopen(NULL, \"a+b\")\n", stdout);
	errno = 0;
	FILE* v1050 = fopen(NULL, "a+b");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1050, __LINE__);
	return failed;
}

int variant69() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[70/148]fopen(File name, \"a+b\")\n", stdout);
	errno = 0;
	FILE* v1051 = fopen("file75", "a+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfa5dc260, 0x00000000, 0, 0, 0x00000000, "", v1051, __LINE__);
	return failed;
}

int variant70() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[71/148]fopen(Empty file, \"a+b\")\n", stdout);
	errno = 0;
	FILE* v1052 = fopen("empty75", "a+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf9290260, 0x00000000, 0, 0, 0x00000000, "", v1052, __LINE__);
	return failed;
}

int variant71() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[72/148]fopen(File with random text, \"a+b\")\n", stdout);
	errno = 0;
	FILE* v1053 = fopen("random75", "a+b");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd40e6260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1053, __LINE__);
	return failed;
}

int variant72() {
	int failed = 0;
	failed = 0;
	fputs("[73/148]fopen(NULL, \"rx\")\n", stdout);
	errno = 0;
	FILE* v1054 = fopen(NULL, "rx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1054, __LINE__);
	return failed;
}

int variant73() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[74/148]fopen(File name, \"rx\")\n", stdout);
	errno = 0;
	FILE* v1055 = fopen("file75", "rx");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1055, __LINE__);
	return failed;
}

int variant74() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[75/148]fopen(Empty file, \"rx\")\n", stdout);
	errno = 0;
	FILE* v1056 = fopen("empty75", "rx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2b018260, 0x00000000, 0, 0, 0x00000000, "", v1056, __LINE__);
	return failed;
}

int variant75() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[76/148]fopen(File with random text, \"rx\")\n", stdout);
	errno = 0;
	FILE* v1057 = fopen("random75", "rx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3b1e1260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1057, __LINE__);
	return failed;
}

int variant76() {
	int failed = 0;
	failed = 0;
	fputs("[77/148]fopen(NULL, \"wx\")\n", stdout);
	errno = 0;
	FILE* v1058 = fopen(NULL, "wx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1058, __LINE__);
	return failed;
}

int variant77() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[78/148]fopen(File name, \"wx\")\n", stdout);
	errno = 0;
	FILE* v1059 = fopen("file75", "wx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1ce9d260, 0x00000000, 0, 0, 0x00000000, "", v1059, __LINE__);
	return failed;
}

int variant78() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[79/148]fopen(Empty file, \"wx\")\n", stdout);
	errno = 0;
	FILE* v1060 = fopen("empty75", "wx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1060, __LINE__);
	return failed;
}

int variant79() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[80/148]fopen(File with random text, \"wx\")\n", stdout);
	errno = 0;
	FILE* v1061 = fopen("random75", "wx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1061, __LINE__);
	return failed;
}

int variant80() {
	int failed = 0;
	failed = 0;
	fputs("[81/148]fopen(NULL, \"ax\")\n", stdout);
	errno = 0;
	FILE* v1062 = fopen(NULL, "ax");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1062, __LINE__);
	return failed;
}

int variant81() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[82/148]fopen(File name, \"ax\")\n", stdout);
	errno = 0;
	FILE* v1063 = fopen("file75", "ax");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x29b10260, 0x00000000, 0, 0, 0x00000000, "", v1063, __LINE__);
	return failed;
}

int variant82() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[83/148]fopen(Empty file, \"ax\")\n", stdout);
	errno = 0;
	FILE* v1064 = fopen("empty75", "ax");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1064, __LINE__);
	return failed;
}

int variant83() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[84/148]fopen(File with random text, \"ax\")\n", stdout);
	errno = 0;
	FILE* v1065 = fopen("random75", "ax");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1065, __LINE__);
	return failed;
}

int variant84() {
	int failed = 0;
	failed = 0;
	fputs("[85/148]fopen(NULL, \"r+x\")\n", stdout);
	errno = 0;
	FILE* v1066 = fopen(NULL, "r+x");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1066, __LINE__);
	return failed;
}

int variant85() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[86/148]fopen(File name, \"r+x\")\n", stdout);
	errno = 0;
	FILE* v1067 = fopen("file75", "r+x");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1067, __LINE__);
	return failed;
}

int variant86() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[87/148]fopen(Empty file, \"r+x\")\n", stdout);
	errno = 0;
	FILE* v1068 = fopen("empty75", "r+x");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x55300260, 0x00000000, 0, 0, 0x00000000, "", v1068, __LINE__);
	return failed;
}

int variant87() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[88/148]fopen(File with random text, \"r+x\")\n", stdout);
	errno = 0;
	FILE* v1069 = fopen("random75", "r+x");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb0fa7260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1069, __LINE__);
	return failed;
}

int variant88() {
	int failed = 0;
	failed = 0;
	fputs("[89/148]fopen(NULL, \"w+x\")\n", stdout);
	errno = 0;
	FILE* v1070 = fopen(NULL, "w+x");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1070, __LINE__);
	return failed;
}

int variant89() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[90/148]fopen(File name, \"w+x\")\n", stdout);
	errno = 0;
	FILE* v1071 = fopen("file75", "w+x");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8ec2f260, 0x00000000, 0, 0, 0x00000000, "", v1071, __LINE__);
	return failed;
}

int variant90() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[91/148]fopen(Empty file, \"w+x\")\n", stdout);
	errno = 0;
	FILE* v1072 = fopen("empty75", "w+x");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1072, __LINE__);
	return failed;
}

int variant91() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[92/148]fopen(File with random text, \"w+x\")\n", stdout);
	errno = 0;
	FILE* v1073 = fopen("random75", "w+x");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1073, __LINE__);
	return failed;
}

int variant92() {
	int failed = 0;
	failed = 0;
	fputs("[93/148]fopen(NULL, \"a+x\")\n", stdout);
	errno = 0;
	FILE* v1074 = fopen(NULL, "a+x");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1074, __LINE__);
	return failed;
}

int variant93() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[94/148]fopen(File name, \"a+x\")\n", stdout);
	errno = 0;
	FILE* v1075 = fopen("file75", "a+x");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x335fe260, 0x00000000, 0, 0, 0x00000000, "", v1075, __LINE__);
	return failed;
}

int variant94() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[95/148]fopen(Empty file, \"a+x\")\n", stdout);
	errno = 0;
	FILE* v1076 = fopen("empty75", "a+x");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1076, __LINE__);
	return failed;
}

int variant95() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[96/148]fopen(File with random text, \"a+x\")\n", stdout);
	errno = 0;
	FILE* v1077 = fopen("random75", "a+x");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1077, __LINE__);
	return failed;
}

int variant96() {
	int failed = 0;
	failed = 0;
	fputs("[97/148]fopen(NULL, \"rtx\")\n", stdout);
	errno = 0;
	FILE* v1078 = fopen(NULL, "rtx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1078, __LINE__);
	return failed;
}

int variant97() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[98/148]fopen(File name, \"rtx\")\n", stdout);
	errno = 0;
	FILE* v1079 = fopen("file75", "rtx");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1079, __LINE__);
	return failed;
}

int variant98() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[99/148]fopen(Empty file, \"rtx\")\n", stdout);
	errno = 0;
	FILE* v1080 = fopen("empty75", "rtx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x17598260, 0x00000000, 0, 0, 0x00000000, "", v1080, __LINE__);
	return failed;
}

int variant99() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[100/148]fopen(File with random text, \"rtx\")\n", stdout);
	errno = 0;
	FILE* v1081 = fopen("random75", "rtx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2db8e260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1081, __LINE__);
	return failed;
}

int variant100() {
	int failed = 0;
	failed = 0;
	fputs("[101/148]fopen(NULL, \"wtx\")\n", stdout);
	errno = 0;
	FILE* v1082 = fopen(NULL, "wtx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1082, __LINE__);
	return failed;
}

int variant101() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[102/148]fopen(File name, \"wtx\")\n", stdout);
	errno = 0;
	FILE* v1083 = fopen("file75", "wtx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9ad7a260, 0x00000000, 0, 0, 0x00000000, "", v1083, __LINE__);
	return failed;
}

int variant102() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[103/148]fopen(Empty file, \"wtx\")\n", stdout);
	errno = 0;
	FILE* v1084 = fopen("empty75", "wtx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1084, __LINE__);
	return failed;
}

int variant103() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[104/148]fopen(File with random text, \"wtx\")\n", stdout);
	errno = 0;
	FILE* v1085 = fopen("random75", "wtx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1085, __LINE__);
	return failed;
}

int variant104() {
	int failed = 0;
	failed = 0;
	fputs("[105/148]fopen(NULL, \"atx\")\n", stdout);
	errno = 0;
	FILE* v1086 = fopen(NULL, "atx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1086, __LINE__);
	return failed;
}

int variant105() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[106/148]fopen(File name, \"atx\")\n", stdout);
	errno = 0;
	FILE* v1087 = fopen("file75", "atx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x44daa260, 0x00000000, 0, 0, 0x00000000, "", v1087, __LINE__);
	return failed;
}

int variant106() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[107/148]fopen(Empty file, \"atx\")\n", stdout);
	errno = 0;
	FILE* v1088 = fopen("empty75", "atx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1088, __LINE__);
	return failed;
}

int variant107() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[108/148]fopen(File with random text, \"atx\")\n", stdout);
	errno = 0;
	FILE* v1089 = fopen("random75", "atx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1089, __LINE__);
	return failed;
}

int variant108() {
	int failed = 0;
	failed = 0;
	fputs("[109/148]fopen(NULL, \"r+tx\")\n", stdout);
	errno = 0;
	FILE* v1090 = fopen(NULL, "r+tx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1090, __LINE__);
	return failed;
}

int variant109() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[110/148]fopen(File name, \"r+tx\")\n", stdout);
	errno = 0;
	FILE* v1091 = fopen("file75", "r+tx");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1091, __LINE__);
	return failed;
}

int variant110() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[111/148]fopen(Empty file, \"r+tx\")\n", stdout);
	errno = 0;
	FILE* v1092 = fopen("empty75", "r+tx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc63e0260, 0x00000000, 0, 0, 0x00000000, "", v1092, __LINE__);
	return failed;
}

int variant111() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[112/148]fopen(File with random text, \"r+tx\")\n", stdout);
	errno = 0;
	FILE* v1093 = fopen("random75", "r+tx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x46a8b260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1093, __LINE__);
	return failed;
}

int variant112() {
	int failed = 0;
	failed = 0;
	fputs("[113/148]fopen(NULL, \"w+tx\")\n", stdout);
	errno = 0;
	FILE* v1094 = fopen(NULL, "w+tx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1094, __LINE__);
	return failed;
}

int variant113() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[114/148]fopen(File name, \"w+tx\")\n", stdout);
	errno = 0;
	FILE* v1095 = fopen("file75", "w+tx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1f4fb260, 0x00000000, 0, 0, 0x00000000, "", v1095, __LINE__);
	return failed;
}

int variant114() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[115/148]fopen(Empty file, \"w+tx\")\n", stdout);
	errno = 0;
	FILE* v1096 = fopen("empty75", "w+tx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1096, __LINE__);
	return failed;
}

int variant115() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[116/148]fopen(File with random text, \"w+tx\")\n", stdout);
	errno = 0;
	FILE* v1097 = fopen("random75", "w+tx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1097, __LINE__);
	return failed;
}

int variant116() {
	int failed = 0;
	failed = 0;
	fputs("[117/148]fopen(NULL, \"a+tx\")\n", stdout);
	errno = 0;
	FILE* v1098 = fopen(NULL, "a+tx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1098, __LINE__);
	return failed;
}

int variant117() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[118/148]fopen(File name, \"a+tx\")\n", stdout);
	errno = 0;
	FILE* v1099 = fopen("file75", "a+tx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0bed7260, 0x00000000, 0, 0, 0x00000000, "", v1099, __LINE__);
	return failed;
}

int variant118() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[119/148]fopen(Empty file, \"a+tx\")\n", stdout);
	errno = 0;
	FILE* v1100 = fopen("empty75", "a+tx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1100, __LINE__);
	return failed;
}

int variant119() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[120/148]fopen(File with random text, \"a+tx\")\n", stdout);
	errno = 0;
	FILE* v1101 = fopen("random75", "a+tx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1101, __LINE__);
	return failed;
}

int variant120() {
	int failed = 0;
	failed = 0;
	fputs("[121/148]fopen(NULL, \"rbx\")\n", stdout);
	errno = 0;
	FILE* v1102 = fopen(NULL, "rbx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1102, __LINE__);
	return failed;
}

int variant121() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[122/148]fopen(File name, \"rbx\")\n", stdout);
	errno = 0;
	FILE* v1103 = fopen("file75", "rbx");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1103, __LINE__);
	return failed;
}

int variant122() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[123/148]fopen(Empty file, \"rbx\")\n", stdout);
	errno = 0;
	FILE* v1104 = fopen("empty75", "rbx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd5e77260, 0x00000000, 0, 0, 0x00000000, "", v1104, __LINE__);
	return failed;
}

int variant123() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[124/148]fopen(File with random text, \"rbx\")\n", stdout);
	errno = 0;
	FILE* v1105 = fopen("random75", "rbx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdfa10260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1105, __LINE__);
	return failed;
}

int variant124() {
	int failed = 0;
	failed = 0;
	fputs("[125/148]fopen(NULL, \"wbx\")\n", stdout);
	errno = 0;
	FILE* v1106 = fopen(NULL, "wbx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1106, __LINE__);
	return failed;
}

int variant125() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[126/148]fopen(File name, \"wbx\")\n", stdout);
	errno = 0;
	FILE* v1107 = fopen("file75", "wbx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x79516260, 0x00000000, 0, 0, 0x00000000, "", v1107, __LINE__);
	return failed;
}

int variant126() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[127/148]fopen(Empty file, \"wbx\")\n", stdout);
	errno = 0;
	FILE* v1108 = fopen("empty75", "wbx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1108, __LINE__);
	return failed;
}

int variant127() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[128/148]fopen(File with random text, \"wbx\")\n", stdout);
	errno = 0;
	FILE* v1109 = fopen("random75", "wbx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1109, __LINE__);
	return failed;
}

int variant128() {
	int failed = 0;
	failed = 0;
	fputs("[129/148]fopen(NULL, \"abx\")\n", stdout);
	errno = 0;
	FILE* v1110 = fopen(NULL, "abx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1110, __LINE__);
	return failed;
}

int variant129() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[130/148]fopen(File name, \"abx\")\n", stdout);
	errno = 0;
	FILE* v1111 = fopen("file75", "abx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x640a4260, 0x00000000, 0, 0, 0x00000000, "", v1111, __LINE__);
	return failed;
}

int variant130() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[131/148]fopen(Empty file, \"abx\")\n", stdout);
	errno = 0;
	FILE* v1112 = fopen("empty75", "abx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1112, __LINE__);
	return failed;
}

int variant131() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[132/148]fopen(File with random text, \"abx\")\n", stdout);
	errno = 0;
	FILE* v1113 = fopen("random75", "abx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1113, __LINE__);
	return failed;
}

int variant132() {
	int failed = 0;
	failed = 0;
	fputs("[133/148]fopen(NULL, \"r+bx\")\n", stdout);
	errno = 0;
	FILE* v1114 = fopen(NULL, "r+bx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1114, __LINE__);
	return failed;
}

int variant133() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[134/148]fopen(File name, \"r+bx\")\n", stdout);
	errno = 0;
	FILE* v1115 = fopen("file75", "r+bx");
	failed |= validate_errno(2, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1115, __LINE__);
	return failed;
}

int variant134() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[135/148]fopen(Empty file, \"r+bx\")\n", stdout);
	errno = 0;
	FILE* v1116 = fopen("empty75", "r+bx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2905b260, 0x00000000, 0, 0, 0x00000000, "", v1116, __LINE__);
	return failed;
}

int variant135() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[136/148]fopen(File with random text, \"r+bx\")\n", stdout);
	errno = 0;
	FILE* v1117 = fopen("random75", "r+bx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd4349260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v1117, __LINE__);
	return failed;
}

int variant136() {
	int failed = 0;
	failed = 0;
	fputs("[137/148]fopen(NULL, \"w+bx\")\n", stdout);
	errno = 0;
	FILE* v1118 = fopen(NULL, "w+bx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1118, __LINE__);
	return failed;
}

int variant137() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[138/148]fopen(File name, \"w+bx\")\n", stdout);
	errno = 0;
	FILE* v1119 = fopen("file75", "w+bx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xaef83260, 0x00000000, 0, 0, 0x00000000, "", v1119, __LINE__);
	return failed;
}

int variant138() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[139/148]fopen(Empty file, \"w+bx\")\n", stdout);
	errno = 0;
	FILE* v1120 = fopen("empty75", "w+bx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1120, __LINE__);
	return failed;
}

int variant139() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[140/148]fopen(File with random text, \"w+bx\")\n", stdout);
	errno = 0;
	FILE* v1121 = fopen("random75", "w+bx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1121, __LINE__);
	return failed;
}

int variant140() {
	int failed = 0;
	failed = 0;
	fputs("[141/148]fopen(NULL, \"a+bx\")\n", stdout);
	errno = 0;
	FILE* v1122 = fopen(NULL, "a+bx");
	failed |= validate_errno(14, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1122, __LINE__);
	return failed;
}

int variant141() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[142/148]fopen(File name, \"a+bx\")\n", stdout);
	errno = 0;
	FILE* v1123 = fopen("file75", "a+bx");
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5030c260, 0x00000000, 0, 0, 0x00000000, "", v1123, __LINE__);
	return failed;
}

int variant142() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[143/148]fopen(Empty file, \"a+bx\")\n", stdout);
	errno = 0;
	FILE* v1124 = fopen("empty75", "a+bx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1124, __LINE__);
	return failed;
}

int variant143() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[144/148]fopen(File with random text, \"a+bx\")\n", stdout);
	errno = 0;
	FILE* v1125 = fopen("random75", "a+bx");
	failed |= validate_errno(17, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1125, __LINE__);
	return failed;
}

int variant144() {
	int failed = 0;
	failed = 0;
	fputs("[145/148]fopen(NULL, \"qqq\")\n", stdout);
	errno = 0;
	FILE* v1126 = fopen(NULL, "qqq");
	failed |= validate_errno(22, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1126, __LINE__);
	return failed;
}

int variant145() {
	int failed = 0;
	remove_file("file75");
	failed = 0;
	fputs("[146/148]fopen(File name, \"qqq\")\n", stdout);
	errno = 0;
	FILE* v1127 = fopen("file75", "qqq");
	failed |= validate_errno(22, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1127, __LINE__);
	return failed;
}

int variant146() {
	int failed = 0;
	create_empty_file("empty75");
	failed = 0;
	fputs("[147/148]fopen(Empty file, \"qqq\")\n", stdout);
	errno = 0;
	FILE* v1128 = fopen("empty75", "qqq");
	failed |= validate_errno(22, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1128, __LINE__);
	return failed;
}

int variant147() {
	int failed = 0;
	create_random_file("random75");
	failed = 0;
	fputs("[148/148]fopen(File with random text, \"qqq\")\n", stdout);
	errno = 0;
	FILE* v1129 = fopen("random75", "qqq");
	failed |= validate_errno(22, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v1129, __LINE__);
	return failed;
}

int segfault0() {
	int exitcode = 0;
	if (fork() == 0) {
		FILE* v978 = fopen(NULL, NULL);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[1/4]fopen(NULL, NULL)\n", stdout);
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
		remove_file("file75");
		FILE* v979 = fopen("file75", NULL);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[2/4]fopen(File name, NULL)\n", stdout);
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
		create_empty_file("empty75");
		FILE* v980 = fopen("empty75", NULL);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[3/4]fopen(Empty file, NULL)\n", stdout);
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
		create_random_file("random75");
		FILE* v981 = fopen("random75", NULL);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[4/4]fopen(File with random text, NULL)\n", stdout);
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
	error |= variant72();
	error |= variant73();
	error |= variant74();
	error |= variant75();
	error |= variant76();
	error |= variant77();
	error |= variant78();
	error |= variant79();
	error |= variant80();
	error |= variant81();
	error |= variant82();
	error |= variant83();
	error |= variant84();
	error |= variant85();
	error |= variant86();
	error |= variant87();
	error |= variant88();
	error |= variant89();
	error |= variant90();
	error |= variant91();
	error |= variant92();
	error |= variant93();
	error |= variant94();
	error |= variant95();
	error |= variant96();
	error |= variant97();
	error |= variant98();
	error |= variant99();
	error |= variant100();
	error |= variant101();
	error |= variant102();
	error |= variant103();
	error |= variant104();
	error |= variant105();
	error |= variant106();
	error |= variant107();
	error |= variant108();
	error |= variant109();
	error |= variant110();
	error |= variant111();
	error |= variant112();
	error |= variant113();
	error |= variant114();
	error |= variant115();
	error |= variant116();
	error |= variant117();
	error |= variant118();
	error |= variant119();
	error |= variant120();
	error |= variant121();
	error |= variant122();
	error |= variant123();
	error |= variant124();
	error |= variant125();
	error |= variant126();
	error |= variant127();
	error |= variant128();
	error |= variant129();
	error |= variant130();
	error |= variant131();
	error |= variant132();
	error |= variant133();
	error |= variant134();
	error |= variant135();
	error |= variant136();
	error |= variant137();
	error |= variant138();
	error |= variant139();
	error |= variant140();
	error |= variant141();
	error |= variant142();
	error |= variant143();
	error |= variant144();
	error |= variant145();
	error |= variant146();
	error |= variant147();
	error |= segfault0();
	error |= segfault1();
	error |= segfault2();
	error |= segfault3();
	return error;
}

