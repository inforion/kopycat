#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <string.h>
#include "builtins.h"

int variant0() {
	int failed = 0;
	failed = 0;
	fputs("[1/432]fgets(NULL, Int(0), NULL)\n", stdout);
	errno = 0;
	char* v1348 = fgets(NULL, 0, NULL);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", NULL, __LINE__);
	return failed;
}

int variant1() {
	int failed = 0;
	remove_file("file149");
	FILE* v435 = fopen("file149", "r");
	failed = 0;
	fputs("[2/432]fgets(Random text, Int(0), FILE* (File name, \"r\"))\n", stdout);
	errno = 0;
	char* v1349 = fgets("TwuRXhbUh9As9qqYVuDUYECRl3f1HTyXpEONJBPRAWPzBOYwxprBvBKJny1tQQtPgZB40DV_IcSP_AWAhUjfIanqul5IzwNUyaSC", 0, v435);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v435, __LINE__);
	return failed;
}

int variant2() {
	int failed = 0;
	create_empty_file("empty149");
	FILE* v436 = fopen("empty149", "r");
	failed = 0;
	fputs("[3/432]fgets(NULL, Int(0), FILE* (Empty file, \"r\"))\n", stdout);
	errno = 0;
	char* v1353 = fgets(NULL, 0, v436);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x64c02260, 0x00000000, 0, 0, 0x00000000, "", v436, __LINE__);
	return failed;
}

int variant3() {
	int failed = 0;
	create_empty_file("empty149");
	FILE* v436 = fopen("empty149", "r");
	failed = 0;
	fputs("[4/432]fgets(Random text, Int(0), FILE* (Empty file, \"r\"))\n", stdout);
	errno = 0;
	char* v1354 = fgets("ZmEW1mQMfosPrmWmL_2seCgVkS0FrR3xAp4vsl2ra0gAO5YuPYMidA2THuQxePdOAl8iH9lfwLwCMyTk1sAtpQYGapHi6RTSAcS5", 0, v436);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa4404260, 0x00000000, 0, 0, 0x00000000, "", v436, __LINE__);
	return failed;
}

int variant4() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty149");
	FILE* v436 = fopen("empty149", "r");
	failed = 0;
	fputs("[5/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r\"))\n", stdout);
	errno = 0;
	char* v1355 = fgets(v432, 64, v436);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf84182b0, 0x00000000, 1, 0, 0x00000000, "", v436, __LINE__);
	return failed;
}

int variant5() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty149");
	FILE* v436 = fopen("empty149", "r");
	failed = 0;
	fputs("[6/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r\"))\n", stdout);
	errno = 0;
	char* v1356 = fgets(v433, 64, v436);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1bfe22b0, 0x00000000, 1, 0, 0x00000000, "", v436, __LINE__);
	return failed;
}

int variant6() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty149");
	FILE* v436 = fopen("empty149", "r");
	failed = 0;
	fputs("[7/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r\"))\n", stdout);
	errno = 0;
	char* v1357 = fgets(v434, 64, v436);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdf66e2b0, 0x00000000, 1, 0, 0x00000000, "", v436, __LINE__);
	return failed;
}

int variant7() {
	int failed = 0;
	create_random_file("random149");
	FILE* v437 = fopen("random149", "r");
	failed = 0;
	fputs("[8/432]fgets(NULL, Int(0), FILE* (File with random text, \"r\"))\n", stdout);
	errno = 0;
	char* v1358 = fgets(NULL, 0, v437);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0646b260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v437, __LINE__);
	return failed;
}

int variant8() {
	int failed = 0;
	create_random_file("random149");
	FILE* v437 = fopen("random149", "r");
	failed = 0;
	fputs("[9/432]fgets(Random text, Int(0), FILE* (File with random text, \"r\"))\n", stdout);
	errno = 0;
	char* v1359 = fgets("0XrBoNdrrR_JU1PNoP68Wha1mGYI9bwzHwjI1DcwCZqA4k9WqjFuXFQ7KjIm1At1ba0koV9rgn4gD4F0dYc_0vV6vmYL9wOk8Yqt", 0, v437);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x01c1e260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v437, __LINE__);
	return failed;
}

int variant9() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random149");
	FILE* v437 = fopen("random149", "r");
	failed = 0;
	fputs("[10/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r\"))\n", stdout);
	errno = 0;
	char* v1360 = fgets(v432, 64, v437);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xcf16e2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v437, __LINE__);
	return failed;
}

int variant10() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random149");
	FILE* v437 = fopen("random149", "r");
	failed = 0;
	fputs("[11/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r\"))\n", stdout);
	errno = 0;
	char* v1361 = fgets(v433, 64, v437);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2824b2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v437, __LINE__);
	return failed;
}

int variant11() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random149");
	FILE* v437 = fopen("random149", "r");
	failed = 0;
	fputs("[12/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r\"))\n", stdout);
	errno = 0;
	char* v1362 = fgets(v434, 64, v437);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4bdf42b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v437, __LINE__);
	return failed;
}

int variant12() {
	int failed = 0;
	remove_file("file150");
	FILE* v438 = fopen("file150", "w");
	failed = 0;
	fputs("[13/432]fgets(NULL, Int(0), FILE* (File name, \"w\"))\n", stdout);
	errno = 0;
	char* v1363 = fgets(NULL, 0, v438);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9e98a260, 0x00000000, 0, 0, 0x00000000, "", v438, __LINE__);
	return failed;
}

int variant13() {
	int failed = 0;
	remove_file("file150");
	FILE* v438 = fopen("file150", "w");
	failed = 0;
	fputs("[14/432]fgets(Random text, Int(0), FILE* (File name, \"w\"))\n", stdout);
	errno = 0;
	char* v1364 = fgets("qn3w3F4Sj0PSSs3e9WnAOy6ElTkDJYhHieT97ZuxPnYbOW51ohQi2vn3vWgwbQ6hiJo91wNmlJJoWYuVLwvqY363CkKb4rYMOlP5", 0, v438);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x77c84260, 0x00000000, 0, 0, 0x00000000, "", v438, __LINE__);
	return failed;
}

int variant14() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file150");
	FILE* v438 = fopen("file150", "w");
	failed = 0;
	fputs("[15/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w\"))\n", stdout);
	errno = 0;
	char* v1365 = fgets(v432, 64, v438);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x44af82b0, 0x00000000, 0, 1, 0x00000000, "", v438, __LINE__);
	return failed;
}

int variant15() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file150");
	FILE* v438 = fopen("file150", "w");
	failed = 0;
	fputs("[16/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w\"))\n", stdout);
	errno = 0;
	char* v1366 = fgets(v433, 64, v438);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x809832b0, 0x00000000, 0, 1, 0x00000000, "", v438, __LINE__);
	return failed;
}

int variant16() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file150");
	FILE* v438 = fopen("file150", "w");
	failed = 0;
	fputs("[17/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w\"))\n", stdout);
	errno = 0;
	char* v1367 = fgets(v434, 64, v438);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf9a602b0, 0x00000000, 0, 1, 0x00000000, "", v438, __LINE__);
	return failed;
}

int variant17() {
	int failed = 0;
	create_empty_file("empty150");
	FILE* v439 = fopen("empty150", "w");
	failed = 0;
	fputs("[18/432]fgets(NULL, Int(0), FILE* (Empty file, \"w\"))\n", stdout);
	errno = 0;
	char* v1368 = fgets(NULL, 0, v439);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa58af260, 0x00000000, 0, 0, 0x00000000, "", v439, __LINE__);
	return failed;
}

int variant18() {
	int failed = 0;
	create_empty_file("empty150");
	FILE* v439 = fopen("empty150", "w");
	failed = 0;
	fputs("[19/432]fgets(Random text, Int(0), FILE* (Empty file, \"w\"))\n", stdout);
	errno = 0;
	char* v1369 = fgets("34fXot_A4yXJRga1Z7GbeysisFbduJCxKr2r6y8EJgfKaY7kgss7y3ftlTxLBfQnDz7ufSQ9lspPqu2DbRNsnc5qnQOHUTbCebbQ", 0, v439);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7aec7260, 0x00000000, 0, 0, 0x00000000, "", v439, __LINE__);
	return failed;
}

int variant19() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty150");
	FILE* v439 = fopen("empty150", "w");
	failed = 0;
	fputs("[20/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w\"))\n", stdout);
	errno = 0;
	char* v1370 = fgets(v432, 64, v439);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe8f162b0, 0x00000000, 0, 1, 0x00000000, "", v439, __LINE__);
	return failed;
}

int variant20() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty150");
	FILE* v439 = fopen("empty150", "w");
	failed = 0;
	fputs("[21/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w\"))\n", stdout);
	errno = 0;
	char* v1371 = fgets(v433, 64, v439);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd6eed2b0, 0x00000000, 0, 1, 0x00000000, "", v439, __LINE__);
	return failed;
}

int variant21() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty150");
	FILE* v439 = fopen("empty150", "w");
	failed = 0;
	fputs("[22/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w\"))\n", stdout);
	errno = 0;
	char* v1372 = fgets(v434, 64, v439);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x763562b0, 0x00000000, 0, 1, 0x00000000, "", v439, __LINE__);
	return failed;
}

int variant22() {
	int failed = 0;
	create_random_file("random150");
	FILE* v440 = fopen("random150", "w");
	failed = 0;
	fputs("[23/432]fgets(NULL, Int(0), FILE* (File with random text, \"w\"))\n", stdout);
	errno = 0;
	char* v1373 = fgets(NULL, 0, v440);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x027b9260, 0x00000000, 0, 0, 0x00000000, "", v440, __LINE__);
	return failed;
}

int variant23() {
	int failed = 0;
	create_random_file("random150");
	FILE* v440 = fopen("random150", "w");
	failed = 0;
	fputs("[24/432]fgets(Random text, Int(0), FILE* (File with random text, \"w\"))\n", stdout);
	errno = 0;
	char* v1374 = fgets("LnBBKy54JSCr9P2WndR8WcfPC9sRpnGw9mbU5vo2rZ13gVuTWa3J3XmRzK96VzxSs5Ea7PEQ7DoG8C2vNPJBK5rfAjjsqurs1_IG", 0, v440);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4c77d260, 0x00000000, 0, 0, 0x00000000, "", v440, __LINE__);
	return failed;
}

int variant24() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random150");
	FILE* v440 = fopen("random150", "w");
	failed = 0;
	fputs("[25/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w\"))\n", stdout);
	errno = 0;
	char* v1375 = fgets(v432, 64, v440);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb43d32b0, 0x00000000, 0, 1, 0x00000000, "", v440, __LINE__);
	return failed;
}

int variant25() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random150");
	FILE* v440 = fopen("random150", "w");
	failed = 0;
	fputs("[26/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w\"))\n", stdout);
	errno = 0;
	char* v1376 = fgets(v433, 64, v440);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9d5b32b0, 0x00000000, 0, 1, 0x00000000, "", v440, __LINE__);
	return failed;
}

int variant26() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random150");
	FILE* v440 = fopen("random150", "w");
	failed = 0;
	fputs("[27/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w\"))\n", stdout);
	errno = 0;
	char* v1377 = fgets(v434, 64, v440);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x85f302b0, 0x00000000, 0, 1, 0x00000000, "", v440, __LINE__);
	return failed;
}

int variant27() {
	int failed = 0;
	remove_file("file151");
	FILE* v441 = fopen("file151", "a");
	failed = 0;
	fputs("[28/432]fgets(NULL, Int(0), FILE* (File name, \"a\"))\n", stdout);
	errno = 0;
	char* v1378 = fgets(NULL, 0, v441);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd51ae260, 0x00000000, 0, 0, 0x00000000, "", v441, __LINE__);
	return failed;
}

int variant28() {
	int failed = 0;
	remove_file("file151");
	FILE* v441 = fopen("file151", "a");
	failed = 0;
	fputs("[29/432]fgets(Random text, Int(0), FILE* (File name, \"a\"))\n", stdout);
	errno = 0;
	char* v1379 = fgets("78s_USLW5AuNF1taaK2SdcxqJwYL4qmfl0ILZyq5tbdYRxRFiRsupCRB_zdE59ThB3gO1xbbsOlVovCYJjWyzG2Qnwx50SuRT5E0", 0, v441);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9a996260, 0x00000000, 0, 0, 0x00000000, "", v441, __LINE__);
	return failed;
}

int variant29() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file151");
	FILE* v441 = fopen("file151", "a");
	failed = 0;
	fputs("[30/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a\"))\n", stdout);
	errno = 0;
	char* v1380 = fgets(v432, 64, v441);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd73582b0, 0x00000000, 0, 1, 0x00000000, "", v441, __LINE__);
	return failed;
}

int variant30() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file151");
	FILE* v441 = fopen("file151", "a");
	failed = 0;
	fputs("[31/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a\"))\n", stdout);
	errno = 0;
	char* v1381 = fgets(v433, 64, v441);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa2c0a2b0, 0x00000000, 0, 1, 0x00000000, "", v441, __LINE__);
	return failed;
}

int variant31() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file151");
	FILE* v441 = fopen("file151", "a");
	failed = 0;
	fputs("[32/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a\"))\n", stdout);
	errno = 0;
	char* v1382 = fgets(v434, 64, v441);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5d2e42b0, 0x00000000, 0, 1, 0x00000000, "", v441, __LINE__);
	return failed;
}

int variant32() {
	int failed = 0;
	create_empty_file("empty151");
	FILE* v442 = fopen("empty151", "a");
	failed = 0;
	fputs("[33/432]fgets(NULL, Int(0), FILE* (Empty file, \"a\"))\n", stdout);
	errno = 0;
	char* v1383 = fgets(NULL, 0, v442);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x766b9260, 0x00000000, 0, 0, 0x00000000, "", v442, __LINE__);
	return failed;
}

int variant33() {
	int failed = 0;
	create_empty_file("empty151");
	FILE* v442 = fopen("empty151", "a");
	failed = 0;
	fputs("[34/432]fgets(Random text, Int(0), FILE* (Empty file, \"a\"))\n", stdout);
	errno = 0;
	char* v1384 = fgets("8u31B8gKn_3xutsrQrISK0NU2yOPogBbXxyZ8Tn31hNLam5auVOAwzPZK04O4WF7jXUo9PFtqUxwUTQbhr_5m9KLBD4nkc2aNs_O", 0, v442);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x79e07260, 0x00000000, 0, 0, 0x00000000, "", v442, __LINE__);
	return failed;
}

int variant34() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty151");
	FILE* v442 = fopen("empty151", "a");
	failed = 0;
	fputs("[35/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a\"))\n", stdout);
	errno = 0;
	char* v1385 = fgets(v432, 64, v442);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x630bd2b0, 0x00000000, 0, 1, 0x00000000, "", v442, __LINE__);
	return failed;
}

int variant35() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty151");
	FILE* v442 = fopen("empty151", "a");
	failed = 0;
	fputs("[36/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a\"))\n", stdout);
	errno = 0;
	char* v1386 = fgets(v433, 64, v442);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xee0832b0, 0x00000000, 0, 1, 0x00000000, "", v442, __LINE__);
	return failed;
}

int variant36() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty151");
	FILE* v442 = fopen("empty151", "a");
	failed = 0;
	fputs("[37/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a\"))\n", stdout);
	errno = 0;
	char* v1387 = fgets(v434, 64, v442);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x511792b0, 0x00000000, 0, 1, 0x00000000, "", v442, __LINE__);
	return failed;
}

int variant37() {
	int failed = 0;
	create_random_file("random151");
	FILE* v443 = fopen("random151", "a");
	failed = 0;
	fputs("[38/432]fgets(NULL, Int(0), FILE* (File with random text, \"a\"))\n", stdout);
	errno = 0;
	char* v1388 = fgets(NULL, 0, v443);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x94fb1260, 0x00000400, 0, 0, 0x00000400, "", v443, __LINE__);
	return failed;
}

int variant38() {
	int failed = 0;
	create_random_file("random151");
	FILE* v443 = fopen("random151", "a");
	failed = 0;
	fputs("[39/432]fgets(Random text, Int(0), FILE* (File with random text, \"a\"))\n", stdout);
	errno = 0;
	char* v1389 = fgets("PVmzSoTUXl6rtb1T4mmJptwZBrVV1X98S1S2Pr6MjcUrQPetS9aBLOUaAYMR1nFDzByTgiOer416CZ_OYOHCvJPC6u63uM1YatJO", 0, v443);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd41f6260, 0x00000400, 0, 0, 0x00000400, "", v443, __LINE__);
	return failed;
}

int variant39() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random151");
	FILE* v443 = fopen("random151", "a");
	failed = 0;
	fputs("[40/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a\"))\n", stdout);
	errno = 0;
	char* v1390 = fgets(v432, 64, v443);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x418db2b0, 0x00000400, 0, 1, 0x00000400, "", v443, __LINE__);
	return failed;
}

int variant40() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random151");
	FILE* v443 = fopen("random151", "a");
	failed = 0;
	fputs("[41/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a\"))\n", stdout);
	errno = 0;
	char* v1391 = fgets(v433, 64, v443);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x07bf32b0, 0x00000400, 0, 1, 0x00000400, "", v443, __LINE__);
	return failed;
}

int variant41() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random151");
	FILE* v443 = fopen("random151", "a");
	failed = 0;
	fputs("[42/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a\"))\n", stdout);
	errno = 0;
	char* v1392 = fgets(v434, 64, v443);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd695e2b0, 0x00000400, 0, 1, 0x00000400, "", v443, __LINE__);
	return failed;
}

int variant42() {
	int failed = 0;
	remove_file("file152");
	FILE* v444 = fopen("file152", "r+");
	failed = 0;
	fputs("[43/432]fgets(NULL, Int(0), FILE* (File name, \"r+\"))\n", stdout);
	errno = 0;
	char* v1393 = fgets(NULL, 0, v444);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v444, __LINE__);
	return failed;
}

int variant43() {
	int failed = 0;
	remove_file("file152");
	FILE* v444 = fopen("file152", "r+");
	failed = 0;
	fputs("[44/432]fgets(Random text, Int(0), FILE* (File name, \"r+\"))\n", stdout);
	errno = 0;
	char* v1394 = fgets("K33M70OTZlkcLgeDDqUYxScKSfpDao6gjbOnU4sJ3lhCU_1BGoxJam4aRBzpajKqlsN4VD4pGFcqa8AIkWiiMHhoyAvrOfcQ11kb", 0, v444);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v444, __LINE__);
	return failed;
}

int variant44() {
	int failed = 0;
	create_empty_file("empty152");
	FILE* v445 = fopen("empty152", "r+");
	failed = 0;
	fputs("[45/432]fgets(NULL, Int(0), FILE* (Empty file, \"r+\"))\n", stdout);
	errno = 0;
	char* v1398 = fgets(NULL, 0, v445);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x65709260, 0x00000000, 0, 0, 0x00000000, "", v445, __LINE__);
	return failed;
}

int variant45() {
	int failed = 0;
	create_empty_file("empty152");
	FILE* v445 = fopen("empty152", "r+");
	failed = 0;
	fputs("[46/432]fgets(Random text, Int(0), FILE* (Empty file, \"r+\"))\n", stdout);
	errno = 0;
	char* v1399 = fgets("pYZTsBJ2f05vlnA3eGITbkbUQQmv3mLLJnHl_1gFgSwafwiaPYgFeRd_phsOMwxhgWEDzHDrRFSgG7S_5kxolhTYbS62cr1U3cJQ", 0, v445);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf4c49260, 0x00000000, 0, 0, 0x00000000, "", v445, __LINE__);
	return failed;
}

int variant46() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty152");
	FILE* v445 = fopen("empty152", "r+");
	failed = 0;
	fputs("[47/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r+\"))\n", stdout);
	errno = 0;
	char* v1400 = fgets(v432, 64, v445);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x744502b0, 0x00000000, 1, 0, 0x00000000, "", v445, __LINE__);
	return failed;
}

int variant47() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty152");
	FILE* v445 = fopen("empty152", "r+");
	failed = 0;
	fputs("[48/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r+\"))\n", stdout);
	errno = 0;
	char* v1401 = fgets(v433, 64, v445);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x71a632b0, 0x00000000, 1, 0, 0x00000000, "", v445, __LINE__);
	return failed;
}

int variant48() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty152");
	FILE* v445 = fopen("empty152", "r+");
	failed = 0;
	fputs("[49/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r+\"))\n", stdout);
	errno = 0;
	char* v1402 = fgets(v434, 64, v445);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x69e4f2b0, 0x00000000, 1, 0, 0x00000000, "", v445, __LINE__);
	return failed;
}

int variant49() {
	int failed = 0;
	create_random_file("random152");
	FILE* v446 = fopen("random152", "r+");
	failed = 0;
	fputs("[50/432]fgets(NULL, Int(0), FILE* (File with random text, \"r+\"))\n", stdout);
	errno = 0;
	char* v1403 = fgets(NULL, 0, v446);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x05524260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v446, __LINE__);
	return failed;
}

int variant50() {
	int failed = 0;
	create_random_file("random152");
	FILE* v446 = fopen("random152", "r+");
	failed = 0;
	fputs("[51/432]fgets(Random text, Int(0), FILE* (File with random text, \"r+\"))\n", stdout);
	errno = 0;
	char* v1404 = fgets("TxDfI7OF568mHcpp5e20oC5l5CXcrgQgsfq08LpiajfDB7kdp4c6sT97F2lG9oBuhOcFEmi6bauTEcLUHmnbWgHMKEFxDezUVuQb", 0, v446);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb7508260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v446, __LINE__);
	return failed;
}

int variant51() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random152");
	FILE* v446 = fopen("random152", "r+");
	failed = 0;
	fputs("[52/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r+\"))\n", stdout);
	errno = 0;
	char* v1405 = fgets(v432, 64, v446);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7b6e52b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v446, __LINE__);
	return failed;
}

int variant52() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random152");
	FILE* v446 = fopen("random152", "r+");
	failed = 0;
	fputs("[53/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r+\"))\n", stdout);
	errno = 0;
	char* v1406 = fgets(v433, 64, v446);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb3c532b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v446, __LINE__);
	return failed;
}

int variant53() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random152");
	FILE* v446 = fopen("random152", "r+");
	failed = 0;
	fputs("[54/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r+\"))\n", stdout);
	errno = 0;
	char* v1407 = fgets(v434, 64, v446);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfc76f2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v446, __LINE__);
	return failed;
}

int variant54() {
	int failed = 0;
	remove_file("file153");
	FILE* v447 = fopen("file153", "w+");
	failed = 0;
	fputs("[55/432]fgets(NULL, Int(0), FILE* (File name, \"w+\"))\n", stdout);
	errno = 0;
	char* v1408 = fgets(NULL, 0, v447);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x885a5260, 0x00000000, 0, 0, 0x00000000, "", v447, __LINE__);
	return failed;
}

int variant55() {
	int failed = 0;
	remove_file("file153");
	FILE* v447 = fopen("file153", "w+");
	failed = 0;
	fputs("[56/432]fgets(Random text, Int(0), FILE* (File name, \"w+\"))\n", stdout);
	errno = 0;
	char* v1409 = fgets("oI1x7fbKjgmbhxUucQE4giOY7eVGaQHO3o77Sz0fl4rCZkWm8Wk_rVYXLIiwZFDoZ5wB8zYh_01gxBQ0nb6dDEehAQL5SVOoTcn0", 0, v447);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5b775260, 0x00000000, 0, 0, 0x00000000, "", v447, __LINE__);
	return failed;
}

int variant56() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file153");
	FILE* v447 = fopen("file153", "w+");
	failed = 0;
	fputs("[57/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w+\"))\n", stdout);
	errno = 0;
	char* v1410 = fgets(v432, 64, v447);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3a5382b0, 0x00000000, 1, 0, 0x00000000, "", v447, __LINE__);
	return failed;
}

int variant57() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file153");
	FILE* v447 = fopen("file153", "w+");
	failed = 0;
	fputs("[58/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w+\"))\n", stdout);
	errno = 0;
	char* v1411 = fgets(v433, 64, v447);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9b4bf2b0, 0x00000000, 1, 0, 0x00000000, "", v447, __LINE__);
	return failed;
}

int variant58() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file153");
	FILE* v447 = fopen("file153", "w+");
	failed = 0;
	fputs("[59/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w+\"))\n", stdout);
	errno = 0;
	char* v1412 = fgets(v434, 64, v447);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4e85e2b0, 0x00000000, 1, 0, 0x00000000, "", v447, __LINE__);
	return failed;
}

int variant59() {
	int failed = 0;
	create_empty_file("empty153");
	FILE* v448 = fopen("empty153", "w+");
	failed = 0;
	fputs("[60/432]fgets(NULL, Int(0), FILE* (Empty file, \"w+\"))\n", stdout);
	errno = 0;
	char* v1413 = fgets(NULL, 0, v448);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc6f6b260, 0x00000000, 0, 0, 0x00000000, "", v448, __LINE__);
	return failed;
}

int variant60() {
	int failed = 0;
	create_empty_file("empty153");
	FILE* v448 = fopen("empty153", "w+");
	failed = 0;
	fputs("[61/432]fgets(Random text, Int(0), FILE* (Empty file, \"w+\"))\n", stdout);
	errno = 0;
	char* v1414 = fgets("zpjll6Zx9tKWQ3vEQjw5JdlmQlEDsqGiU_P5dVgykdZDeNfUVd5BFCd7TnA6nB5LPSB9RyHUulHMLwGUirWsj59OaDsSD1f4s_NV", 0, v448);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa7999260, 0x00000000, 0, 0, 0x00000000, "", v448, __LINE__);
	return failed;
}

int variant61() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty153");
	FILE* v448 = fopen("empty153", "w+");
	failed = 0;
	fputs("[62/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w+\"))\n", stdout);
	errno = 0;
	char* v1415 = fgets(v432, 64, v448);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x847d12b0, 0x00000000, 1, 0, 0x00000000, "", v448, __LINE__);
	return failed;
}

int variant62() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty153");
	FILE* v448 = fopen("empty153", "w+");
	failed = 0;
	fputs("[63/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w+\"))\n", stdout);
	errno = 0;
	char* v1416 = fgets(v433, 64, v448);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x01cde2b0, 0x00000000, 1, 0, 0x00000000, "", v448, __LINE__);
	return failed;
}

int variant63() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty153");
	FILE* v448 = fopen("empty153", "w+");
	failed = 0;
	fputs("[64/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w+\"))\n", stdout);
	errno = 0;
	char* v1417 = fgets(v434, 64, v448);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe012e2b0, 0x00000000, 1, 0, 0x00000000, "", v448, __LINE__);
	return failed;
}

int variant64() {
	int failed = 0;
	create_random_file("random153");
	FILE* v449 = fopen("random153", "w+");
	failed = 0;
	fputs("[65/432]fgets(NULL, Int(0), FILE* (File with random text, \"w+\"))\n", stdout);
	errno = 0;
	char* v1418 = fgets(NULL, 0, v449);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x60aef260, 0x00000000, 0, 0, 0x00000000, "", v449, __LINE__);
	return failed;
}

int variant65() {
	int failed = 0;
	create_random_file("random153");
	FILE* v449 = fopen("random153", "w+");
	failed = 0;
	fputs("[66/432]fgets(Random text, Int(0), FILE* (File with random text, \"w+\"))\n", stdout);
	errno = 0;
	char* v1419 = fgets("5ifou3urd0gy6dfSq_B54hnaDtN91FvJFMEaCUMPkv7lhJiXs8JP4cHB9KLL6u0Oq9mcB1BT8KIuHKTOZYzCSsafj54tMMDlO42v", 0, v449);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc2f61260, 0x00000000, 0, 0, 0x00000000, "", v449, __LINE__);
	return failed;
}

int variant66() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random153");
	FILE* v449 = fopen("random153", "w+");
	failed = 0;
	fputs("[67/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w+\"))\n", stdout);
	errno = 0;
	char* v1420 = fgets(v432, 64, v449);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf3e512b0, 0x00000000, 1, 0, 0x00000000, "", v449, __LINE__);
	return failed;
}

int variant67() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random153");
	FILE* v449 = fopen("random153", "w+");
	failed = 0;
	fputs("[68/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w+\"))\n", stdout);
	errno = 0;
	char* v1421 = fgets(v433, 64, v449);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd3c122b0, 0x00000000, 1, 0, 0x00000000, "", v449, __LINE__);
	return failed;
}

int variant68() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random153");
	FILE* v449 = fopen("random153", "w+");
	failed = 0;
	fputs("[69/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w+\"))\n", stdout);
	errno = 0;
	char* v1422 = fgets(v434, 64, v449);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfc9942b0, 0x00000000, 1, 0, 0x00000000, "", v449, __LINE__);
	return failed;
}

int variant69() {
	int failed = 0;
	remove_file("file154");
	FILE* v450 = fopen("file154", "a+");
	failed = 0;
	fputs("[70/432]fgets(NULL, Int(0), FILE* (File name, \"a+\"))\n", stdout);
	errno = 0;
	char* v1423 = fgets(NULL, 0, v450);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x88908260, 0x00000000, 0, 0, 0x00000000, "", v450, __LINE__);
	return failed;
}

int variant70() {
	int failed = 0;
	remove_file("file154");
	FILE* v450 = fopen("file154", "a+");
	failed = 0;
	fputs("[71/432]fgets(Random text, Int(0), FILE* (File name, \"a+\"))\n", stdout);
	errno = 0;
	char* v1424 = fgets("wT8nmXgZtapXSLQs4fbjrBPPrVili9ArA9JJM3qejNkVXdCUgzXlvg7_r2OFRYoi6TWPySPPzHZBN_AIsNLvI3QQxrFORDz4MkFf", 0, v450);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x47e93260, 0x00000000, 0, 0, 0x00000000, "", v450, __LINE__);
	return failed;
}

int variant71() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file154");
	FILE* v450 = fopen("file154", "a+");
	failed = 0;
	fputs("[72/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a+\"))\n", stdout);
	errno = 0;
	char* v1425 = fgets(v432, 64, v450);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3983f2b0, 0x00000000, 1, 0, 0x00000000, "", v450, __LINE__);
	return failed;
}

int variant72() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file154");
	FILE* v450 = fopen("file154", "a+");
	failed = 0;
	fputs("[73/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a+\"))\n", stdout);
	errno = 0;
	char* v1426 = fgets(v433, 64, v450);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3c6602b0, 0x00000000, 1, 0, 0x00000000, "", v450, __LINE__);
	return failed;
}

int variant73() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file154");
	FILE* v450 = fopen("file154", "a+");
	failed = 0;
	fputs("[74/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a+\"))\n", stdout);
	errno = 0;
	char* v1427 = fgets(v434, 64, v450);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x119012b0, 0x00000000, 1, 0, 0x00000000, "", v450, __LINE__);
	return failed;
}

int variant74() {
	int failed = 0;
	create_empty_file("empty154");
	FILE* v451 = fopen("empty154", "a+");
	failed = 0;
	fputs("[75/432]fgets(NULL, Int(0), FILE* (Empty file, \"a+\"))\n", stdout);
	errno = 0;
	char* v1428 = fgets(NULL, 0, v451);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x15a81260, 0x00000000, 0, 0, 0x00000000, "", v451, __LINE__);
	return failed;
}

int variant75() {
	int failed = 0;
	create_empty_file("empty154");
	FILE* v451 = fopen("empty154", "a+");
	failed = 0;
	fputs("[76/432]fgets(Random text, Int(0), FILE* (Empty file, \"a+\"))\n", stdout);
	errno = 0;
	char* v1429 = fgets("QAgjOMS0Rt76sCTi1HTigsD_rnPXfyK7Jm54WsTEH44UAl3VbzcqRvE_u0qrXNfpesMgEtt29sJHeHUbq1ZYD1KapGyVGXzknpe_", 0, v451);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x41262260, 0x00000000, 0, 0, 0x00000000, "", v451, __LINE__);
	return failed;
}

int variant76() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty154");
	FILE* v451 = fopen("empty154", "a+");
	failed = 0;
	fputs("[77/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a+\"))\n", stdout);
	errno = 0;
	char* v1430 = fgets(v432, 64, v451);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x586d02b0, 0x00000000, 1, 0, 0x00000000, "", v451, __LINE__);
	return failed;
}

int variant77() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty154");
	FILE* v451 = fopen("empty154", "a+");
	failed = 0;
	fputs("[78/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a+\"))\n", stdout);
	errno = 0;
	char* v1431 = fgets(v433, 64, v451);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x19ff32b0, 0x00000000, 1, 0, 0x00000000, "", v451, __LINE__);
	return failed;
}

int variant78() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty154");
	FILE* v451 = fopen("empty154", "a+");
	failed = 0;
	fputs("[79/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a+\"))\n", stdout);
	errno = 0;
	char* v1432 = fgets(v434, 64, v451);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x98e9c2b0, 0x00000000, 1, 0, 0x00000000, "", v451, __LINE__);
	return failed;
}

int variant79() {
	int failed = 0;
	create_random_file("random154");
	FILE* v452 = fopen("random154", "a+");
	failed = 0;
	fputs("[80/432]fgets(NULL, Int(0), FILE* (File with random text, \"a+\"))\n", stdout);
	errno = 0;
	char* v1433 = fgets(NULL, 0, v452);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf15b3260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v452, __LINE__);
	return failed;
}

int variant80() {
	int failed = 0;
	create_random_file("random154");
	FILE* v452 = fopen("random154", "a+");
	failed = 0;
	fputs("[81/432]fgets(Random text, Int(0), FILE* (File with random text, \"a+\"))\n", stdout);
	errno = 0;
	char* v1434 = fgets("qSGoqA0Iaj1zCH8f_u9zeBhRFeYy9BDeoZjsLcTFsQt4XYfJuylRa0GODQnjdSfSXII3Q7gW94V7E62axAqZXRt5HekI8KX12wAl", 0, v452);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x48e1d260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v452, __LINE__);
	return failed;
}

int variant81() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random154");
	FILE* v452 = fopen("random154", "a+");
	failed = 0;
	fputs("[82/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a+\"))\n", stdout);
	errno = 0;
	char* v1435 = fgets(v432, 64, v452);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x12a932b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v452, __LINE__);
	return failed;
}

int variant82() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random154");
	FILE* v452 = fopen("random154", "a+");
	failed = 0;
	fputs("[83/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a+\"))\n", stdout);
	errno = 0;
	char* v1436 = fgets(v433, 64, v452);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x928f32b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v452, __LINE__);
	return failed;
}

int variant83() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random154");
	FILE* v452 = fopen("random154", "a+");
	failed = 0;
	fputs("[84/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a+\"))\n", stdout);
	errno = 0;
	char* v1437 = fgets(v434, 64, v452);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x40add2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v452, __LINE__);
	return failed;
}

int variant84() {
	int failed = 0;
	remove_file("file155");
	FILE* v453 = fopen("file155", "rt");
	failed = 0;
	fputs("[85/432]fgets(NULL, Int(0), FILE* (File name, \"rt\"))\n", stdout);
	errno = 0;
	char* v1438 = fgets(NULL, 0, v453);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v453, __LINE__);
	return failed;
}

int variant85() {
	int failed = 0;
	remove_file("file155");
	FILE* v453 = fopen("file155", "rt");
	failed = 0;
	fputs("[86/432]fgets(Random text, Int(0), FILE* (File name, \"rt\"))\n", stdout);
	errno = 0;
	char* v1439 = fgets("smIZWkmsk2FKIqmc2sb9BIMn6F2Lm1ClnnfXy6XlOIU57gdq8eLUks4JGiwkZMW9QarXCCVbIGgBRMXQdRb6t6yBHo7P1N7R47xo", 0, v453);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v453, __LINE__);
	return failed;
}

int variant86() {
	int failed = 0;
	create_empty_file("empty155");
	FILE* v454 = fopen("empty155", "rt");
	failed = 0;
	fputs("[87/432]fgets(NULL, Int(0), FILE* (Empty file, \"rt\"))\n", stdout);
	errno = 0;
	char* v1443 = fgets(NULL, 0, v454);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb8832260, 0x00000000, 0, 0, 0x00000000, "", v454, __LINE__);
	return failed;
}

int variant87() {
	int failed = 0;
	create_empty_file("empty155");
	FILE* v454 = fopen("empty155", "rt");
	failed = 0;
	fputs("[88/432]fgets(Random text, Int(0), FILE* (Empty file, \"rt\"))\n", stdout);
	errno = 0;
	char* v1444 = fgets("VR9MISwMVEgp5TxqeFQXr0xwQFCawatZoevyMMwH3EG0_ogIz3Ug0i1q2fqTqVzeFoBwErRHRcLkBMRy0wgplmauHzhDiNrdRkup", 0, v454);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd8536260, 0x00000000, 0, 0, 0x00000000, "", v454, __LINE__);
	return failed;
}

int variant88() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty155");
	FILE* v454 = fopen("empty155", "rt");
	failed = 0;
	fputs("[89/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"rt\"))\n", stdout);
	errno = 0;
	char* v1445 = fgets(v432, 64, v454);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa79af2b0, 0x00000000, 1, 0, 0x00000000, "", v454, __LINE__);
	return failed;
}

int variant89() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty155");
	FILE* v454 = fopen("empty155", "rt");
	failed = 0;
	fputs("[90/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"rt\"))\n", stdout);
	errno = 0;
	char* v1446 = fgets(v433, 64, v454);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x73dea2b0, 0x00000000, 1, 0, 0x00000000, "", v454, __LINE__);
	return failed;
}

int variant90() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty155");
	FILE* v454 = fopen("empty155", "rt");
	failed = 0;
	fputs("[91/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"rt\"))\n", stdout);
	errno = 0;
	char* v1447 = fgets(v434, 64, v454);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa30ff2b0, 0x00000000, 1, 0, 0x00000000, "", v454, __LINE__);
	return failed;
}

int variant91() {
	int failed = 0;
	create_random_file("random155");
	FILE* v455 = fopen("random155", "rt");
	failed = 0;
	fputs("[92/432]fgets(NULL, Int(0), FILE* (File with random text, \"rt\"))\n", stdout);
	errno = 0;
	char* v1448 = fgets(NULL, 0, v455);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x78753260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v455, __LINE__);
	return failed;
}

int variant92() {
	int failed = 0;
	create_random_file("random155");
	FILE* v455 = fopen("random155", "rt");
	failed = 0;
	fputs("[93/432]fgets(Random text, Int(0), FILE* (File with random text, \"rt\"))\n", stdout);
	errno = 0;
	char* v1449 = fgets("Mop8YgXBsG2xbQ6neU9MrGe_uv8PgifYQ9OGFE1Bn1bxD52lQauqMQi1WzkKPFqaLpX658AnFSsHOBHCmQKDmlrScCZQtPJtnL5D", 0, v455);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x76e5a260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v455, __LINE__);
	return failed;
}

int variant93() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random155");
	FILE* v455 = fopen("random155", "rt");
	failed = 0;
	fputs("[94/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"rt\"))\n", stdout);
	errno = 0;
	char* v1450 = fgets(v432, 64, v455);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0cf562b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v455, __LINE__);
	return failed;
}

int variant94() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random155");
	FILE* v455 = fopen("random155", "rt");
	failed = 0;
	fputs("[95/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"rt\"))\n", stdout);
	errno = 0;
	char* v1451 = fgets(v433, 64, v455);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbdeee2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v455, __LINE__);
	return failed;
}

int variant95() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random155");
	FILE* v455 = fopen("random155", "rt");
	failed = 0;
	fputs("[96/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"rt\"))\n", stdout);
	errno = 0;
	char* v1452 = fgets(v434, 64, v455);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x666742b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v455, __LINE__);
	return failed;
}

int variant96() {
	int failed = 0;
	remove_file("file156");
	FILE* v456 = fopen("file156", "wt");
	failed = 0;
	fputs("[97/432]fgets(NULL, Int(0), FILE* (File name, \"wt\"))\n", stdout);
	errno = 0;
	char* v1453 = fgets(NULL, 0, v456);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x48e27260, 0x00000000, 0, 0, 0x00000000, "", v456, __LINE__);
	return failed;
}

int variant97() {
	int failed = 0;
	remove_file("file156");
	FILE* v456 = fopen("file156", "wt");
	failed = 0;
	fputs("[98/432]fgets(Random text, Int(0), FILE* (File name, \"wt\"))\n", stdout);
	errno = 0;
	char* v1454 = fgets("aAhBWMi8x9f0QXqd4eb4JKe2LawfCSAj33USPwbjFT25wifoMbccB0ZPtqKcBVxPNkaPfJBN9yFWO3FhWLNLAedLZTE_i2p9rI86", 0, v456);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x121b6260, 0x00000000, 0, 0, 0x00000000, "", v456, __LINE__);
	return failed;
}

int variant98() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file156");
	FILE* v456 = fopen("file156", "wt");
	failed = 0;
	fputs("[99/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"wt\"))\n", stdout);
	errno = 0;
	char* v1455 = fgets(v432, 64, v456);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x728b22b0, 0x00000000, 0, 1, 0x00000000, "", v456, __LINE__);
	return failed;
}

int variant99() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file156");
	FILE* v456 = fopen("file156", "wt");
	failed = 0;
	fputs("[100/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"wt\"))\n", stdout);
	errno = 0;
	char* v1456 = fgets(v433, 64, v456);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x504472b0, 0x00000000, 0, 1, 0x00000000, "", v456, __LINE__);
	return failed;
}

int variant100() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file156");
	FILE* v456 = fopen("file156", "wt");
	failed = 0;
	fputs("[101/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"wt\"))\n", stdout);
	errno = 0;
	char* v1457 = fgets(v434, 64, v456);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1c28a2b0, 0x00000000, 0, 1, 0x00000000, "", v456, __LINE__);
	return failed;
}

int variant101() {
	int failed = 0;
	create_empty_file("empty156");
	FILE* v457 = fopen("empty156", "wt");
	failed = 0;
	fputs("[102/432]fgets(NULL, Int(0), FILE* (Empty file, \"wt\"))\n", stdout);
	errno = 0;
	char* v1458 = fgets(NULL, 0, v457);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4ef5c260, 0x00000000, 0, 0, 0x00000000, "", v457, __LINE__);
	return failed;
}

int variant102() {
	int failed = 0;
	create_empty_file("empty156");
	FILE* v457 = fopen("empty156", "wt");
	failed = 0;
	fputs("[103/432]fgets(Random text, Int(0), FILE* (Empty file, \"wt\"))\n", stdout);
	errno = 0;
	char* v1459 = fgets("f3uzjed2PhALttBBEDcVS5u95z_C7nmVPX9BjMgeeI1NlPfW8ezjDeEudAUNB26yiPWCmacxQqilHdlIzokWCtDKmi8xTOdwcMH2", 0, v457);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa4b14260, 0x00000000, 0, 0, 0x00000000, "", v457, __LINE__);
	return failed;
}

int variant103() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty156");
	FILE* v457 = fopen("empty156", "wt");
	failed = 0;
	fputs("[104/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"wt\"))\n", stdout);
	errno = 0;
	char* v1460 = fgets(v432, 64, v457);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x087452b0, 0x00000000, 0, 1, 0x00000000, "", v457, __LINE__);
	return failed;
}

int variant104() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty156");
	FILE* v457 = fopen("empty156", "wt");
	failed = 0;
	fputs("[105/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"wt\"))\n", stdout);
	errno = 0;
	char* v1461 = fgets(v433, 64, v457);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x731282b0, 0x00000000, 0, 1, 0x00000000, "", v457, __LINE__);
	return failed;
}

int variant105() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty156");
	FILE* v457 = fopen("empty156", "wt");
	failed = 0;
	fputs("[106/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"wt\"))\n", stdout);
	errno = 0;
	char* v1462 = fgets(v434, 64, v457);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5a40c2b0, 0x00000000, 0, 1, 0x00000000, "", v457, __LINE__);
	return failed;
}

int variant106() {
	int failed = 0;
	create_random_file("random156");
	FILE* v458 = fopen("random156", "wt");
	failed = 0;
	fputs("[107/432]fgets(NULL, Int(0), FILE* (File with random text, \"wt\"))\n", stdout);
	errno = 0;
	char* v1463 = fgets(NULL, 0, v458);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xcf994260, 0x00000000, 0, 0, 0x00000000, "", v458, __LINE__);
	return failed;
}

int variant107() {
	int failed = 0;
	create_random_file("random156");
	FILE* v458 = fopen("random156", "wt");
	failed = 0;
	fputs("[108/432]fgets(Random text, Int(0), FILE* (File with random text, \"wt\"))\n", stdout);
	errno = 0;
	char* v1464 = fgets("lp2mpgjhCcxI11PuowXBfHxFWtF0zB8x02j2UF4gfHZgnx4sQ0QgVSZD8ap9DarwAfyHIJbS2YGQyFgRexJYeysQ7XXYdW_mpz8S", 0, v458);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x42813260, 0x00000000, 0, 0, 0x00000000, "", v458, __LINE__);
	return failed;
}

int variant108() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random156");
	FILE* v458 = fopen("random156", "wt");
	failed = 0;
	fputs("[109/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"wt\"))\n", stdout);
	errno = 0;
	char* v1465 = fgets(v432, 64, v458);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf933a2b0, 0x00000000, 0, 1, 0x00000000, "", v458, __LINE__);
	return failed;
}

int variant109() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random156");
	FILE* v458 = fopen("random156", "wt");
	failed = 0;
	fputs("[110/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"wt\"))\n", stdout);
	errno = 0;
	char* v1466 = fgets(v433, 64, v458);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x48fb52b0, 0x00000000, 0, 1, 0x00000000, "", v458, __LINE__);
	return failed;
}

int variant110() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random156");
	FILE* v458 = fopen("random156", "wt");
	failed = 0;
	fputs("[111/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"wt\"))\n", stdout);
	errno = 0;
	char* v1467 = fgets(v434, 64, v458);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x593c62b0, 0x00000000, 0, 1, 0x00000000, "", v458, __LINE__);
	return failed;
}

int variant111() {
	int failed = 0;
	remove_file("file157");
	FILE* v459 = fopen("file157", "at");
	failed = 0;
	fputs("[112/432]fgets(NULL, Int(0), FILE* (File name, \"at\"))\n", stdout);
	errno = 0;
	char* v1468 = fgets(NULL, 0, v459);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb7926260, 0x00000000, 0, 0, 0x00000000, "", v459, __LINE__);
	return failed;
}

int variant112() {
	int failed = 0;
	remove_file("file157");
	FILE* v459 = fopen("file157", "at");
	failed = 0;
	fputs("[113/432]fgets(Random text, Int(0), FILE* (File name, \"at\"))\n", stdout);
	errno = 0;
	char* v1469 = fgets("wD5J_cJXHAxpLCnP5IE_OEk64SMFiNkPeBN_LbNAmEtZiHdevMRxbfnGdDLJrm_UfAhgty3ugkfWnjUhJt1Uq_F1P18km_Jh2Fzm", 0, v459);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x94d59260, 0x00000000, 0, 0, 0x00000000, "", v459, __LINE__);
	return failed;
}

int variant113() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file157");
	FILE* v459 = fopen("file157", "at");
	failed = 0;
	fputs("[114/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"at\"))\n", stdout);
	errno = 0;
	char* v1470 = fgets(v432, 64, v459);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x949e32b0, 0x00000000, 0, 1, 0x00000000, "", v459, __LINE__);
	return failed;
}

int variant114() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file157");
	FILE* v459 = fopen("file157", "at");
	failed = 0;
	fputs("[115/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"at\"))\n", stdout);
	errno = 0;
	char* v1471 = fgets(v433, 64, v459);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0f6052b0, 0x00000000, 0, 1, 0x00000000, "", v459, __LINE__);
	return failed;
}

int variant115() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file157");
	FILE* v459 = fopen("file157", "at");
	failed = 0;
	fputs("[116/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"at\"))\n", stdout);
	errno = 0;
	char* v1472 = fgets(v434, 64, v459);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb692a2b0, 0x00000000, 0, 1, 0x00000000, "", v459, __LINE__);
	return failed;
}

int variant116() {
	int failed = 0;
	create_empty_file("empty157");
	FILE* v460 = fopen("empty157", "at");
	failed = 0;
	fputs("[117/432]fgets(NULL, Int(0), FILE* (Empty file, \"at\"))\n", stdout);
	errno = 0;
	char* v1473 = fgets(NULL, 0, v460);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0e38b260, 0x00000000, 0, 0, 0x00000000, "", v460, __LINE__);
	return failed;
}

int variant117() {
	int failed = 0;
	create_empty_file("empty157");
	FILE* v460 = fopen("empty157", "at");
	failed = 0;
	fputs("[118/432]fgets(Random text, Int(0), FILE* (Empty file, \"at\"))\n", stdout);
	errno = 0;
	char* v1474 = fgets("n3_YzKWIDahkCrw1nKLTsgH3EdwrCgpkeT0gG0tUoPb2BcYKwKKsHnrepz_KZQ24FrKURWrjCyF6jE_eEqpcp6Gup4sZLF0Js1TR", 0, v460);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x80d53260, 0x00000000, 0, 0, 0x00000000, "", v460, __LINE__);
	return failed;
}

int variant118() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty157");
	FILE* v460 = fopen("empty157", "at");
	failed = 0;
	fputs("[119/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"at\"))\n", stdout);
	errno = 0;
	char* v1475 = fgets(v432, 64, v460);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7b9f12b0, 0x00000000, 0, 1, 0x00000000, "", v460, __LINE__);
	return failed;
}

int variant119() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty157");
	FILE* v460 = fopen("empty157", "at");
	failed = 0;
	fputs("[120/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"at\"))\n", stdout);
	errno = 0;
	char* v1476 = fgets(v433, 64, v460);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd57f82b0, 0x00000000, 0, 1, 0x00000000, "", v460, __LINE__);
	return failed;
}

int variant120() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty157");
	FILE* v460 = fopen("empty157", "at");
	failed = 0;
	fputs("[121/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"at\"))\n", stdout);
	errno = 0;
	char* v1477 = fgets(v434, 64, v460);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x723002b0, 0x00000000, 0, 1, 0x00000000, "", v460, __LINE__);
	return failed;
}

int variant121() {
	int failed = 0;
	create_random_file("random157");
	FILE* v461 = fopen("random157", "at");
	failed = 0;
	fputs("[122/432]fgets(NULL, Int(0), FILE* (File with random text, \"at\"))\n", stdout);
	errno = 0;
	char* v1478 = fgets(NULL, 0, v461);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3521d260, 0x00000400, 0, 0, 0x00000400, "", v461, __LINE__);
	return failed;
}

int variant122() {
	int failed = 0;
	create_random_file("random157");
	FILE* v461 = fopen("random157", "at");
	failed = 0;
	fputs("[123/432]fgets(Random text, Int(0), FILE* (File with random text, \"at\"))\n", stdout);
	errno = 0;
	char* v1479 = fgets("6RFHi6PycUkx1QrxIy9Ahl2uJgOVGEICZGmHnKTPmL56yRq2minfgWL3WJ0vQfezwllgXNNw_phlId3O4wCYSMZYUVe0ctrCYtun", 0, v461);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x53230260, 0x00000400, 0, 0, 0x00000400, "", v461, __LINE__);
	return failed;
}

int variant123() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random157");
	FILE* v461 = fopen("random157", "at");
	failed = 0;
	fputs("[124/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"at\"))\n", stdout);
	errno = 0;
	char* v1480 = fgets(v432, 64, v461);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfe9ce2b0, 0x00000400, 0, 1, 0x00000400, "", v461, __LINE__);
	return failed;
}

int variant124() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random157");
	FILE* v461 = fopen("random157", "at");
	failed = 0;
	fputs("[125/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"at\"))\n", stdout);
	errno = 0;
	char* v1481 = fgets(v433, 64, v461);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa24972b0, 0x00000400, 0, 1, 0x00000400, "", v461, __LINE__);
	return failed;
}

int variant125() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random157");
	FILE* v461 = fopen("random157", "at");
	failed = 0;
	fputs("[126/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"at\"))\n", stdout);
	errno = 0;
	char* v1482 = fgets(v434, 64, v461);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6866b2b0, 0x00000400, 0, 1, 0x00000400, "", v461, __LINE__);
	return failed;
}

int variant126() {
	int failed = 0;
	remove_file("file158");
	FILE* v462 = fopen("file158", "r+t");
	failed = 0;
	fputs("[127/432]fgets(NULL, Int(0), FILE* (File name, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1483 = fgets(NULL, 0, v462);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v462, __LINE__);
	return failed;
}

int variant127() {
	int failed = 0;
	remove_file("file158");
	FILE* v462 = fopen("file158", "r+t");
	failed = 0;
	fputs("[128/432]fgets(Random text, Int(0), FILE* (File name, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1484 = fgets("JLUj9HdOP2GGKzvkRVFgUl8JKHbtAaYTcg25zZIUBFZ8WH9FZNu5dD52Rg95Ual7shDE96iXx16UciUYbiDyxjJlYjqPWiwzg6mZ", 0, v462);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v462, __LINE__);
	return failed;
}

int variant128() {
	int failed = 0;
	create_empty_file("empty158");
	FILE* v463 = fopen("empty158", "r+t");
	failed = 0;
	fputs("[129/432]fgets(NULL, Int(0), FILE* (Empty file, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1488 = fgets(NULL, 0, v463);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa5d18260, 0x00000000, 0, 0, 0x00000000, "", v463, __LINE__);
	return failed;
}

int variant129() {
	int failed = 0;
	create_empty_file("empty158");
	FILE* v463 = fopen("empty158", "r+t");
	failed = 0;
	fputs("[130/432]fgets(Random text, Int(0), FILE* (Empty file, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1489 = fgets("l3hcBLm2AZCzFKO10jVdHdR8L2X_xllSVJS80_3EBgzWlJLSKmXr9PZGW2APa1aaH4nQPuUKZDLdW9qkqruLMvwtfqF1F_zmwZwt", 0, v463);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1f768260, 0x00000000, 0, 0, 0x00000000, "", v463, __LINE__);
	return failed;
}

int variant130() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty158");
	FILE* v463 = fopen("empty158", "r+t");
	failed = 0;
	fputs("[131/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1490 = fgets(v432, 64, v463);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa0a342b0, 0x00000000, 1, 0, 0x00000000, "", v463, __LINE__);
	return failed;
}

int variant131() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty158");
	FILE* v463 = fopen("empty158", "r+t");
	failed = 0;
	fputs("[132/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1491 = fgets(v433, 64, v463);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x412c62b0, 0x00000000, 1, 0, 0x00000000, "", v463, __LINE__);
	return failed;
}

int variant132() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty158");
	FILE* v463 = fopen("empty158", "r+t");
	failed = 0;
	fputs("[133/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1492 = fgets(v434, 64, v463);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9ebe62b0, 0x00000000, 1, 0, 0x00000000, "", v463, __LINE__);
	return failed;
}

int variant133() {
	int failed = 0;
	create_random_file("random158");
	FILE* v464 = fopen("random158", "r+t");
	failed = 0;
	fputs("[134/432]fgets(NULL, Int(0), FILE* (File with random text, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1493 = fgets(NULL, 0, v464);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1a39f260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v464, __LINE__);
	return failed;
}

int variant134() {
	int failed = 0;
	create_random_file("random158");
	FILE* v464 = fopen("random158", "r+t");
	failed = 0;
	fputs("[135/432]fgets(Random text, Int(0), FILE* (File with random text, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1494 = fgets("r9FgR9wZyBuDNvjkil7cAoDD7kwIHNRSAljlF2EQRP1NqUymLubHTujWHM2xLUq3NKPFHrOtFq2D86CFTr2KuH0z_WdSdhdV2O93", 0, v464);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x09407260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v464, __LINE__);
	return failed;
}

int variant135() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random158");
	FILE* v464 = fopen("random158", "r+t");
	failed = 0;
	fputs("[136/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1495 = fgets(v432, 64, v464);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6e8632b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v464, __LINE__);
	return failed;
}

int variant136() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random158");
	FILE* v464 = fopen("random158", "r+t");
	failed = 0;
	fputs("[137/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1496 = fgets(v433, 64, v464);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x20d012b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v464, __LINE__);
	return failed;
}

int variant137() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random158");
	FILE* v464 = fopen("random158", "r+t");
	failed = 0;
	fputs("[138/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r+t\"))\n", stdout);
	errno = 0;
	char* v1497 = fgets(v434, 64, v464);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdc15f2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v464, __LINE__);
	return failed;
}

int variant138() {
	int failed = 0;
	remove_file("file159");
	FILE* v465 = fopen("file159", "w+t");
	failed = 0;
	fputs("[139/432]fgets(NULL, Int(0), FILE* (File name, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1498 = fgets(NULL, 0, v465);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x506d4260, 0x00000000, 0, 0, 0x00000000, "", v465, __LINE__);
	return failed;
}

int variant139() {
	int failed = 0;
	remove_file("file159");
	FILE* v465 = fopen("file159", "w+t");
	failed = 0;
	fputs("[140/432]fgets(Random text, Int(0), FILE* (File name, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1499 = fgets("Tw0R8DGoNoHH8Pt0ZEd2lHeVcdykc4XA8eClIWi9JsG5NfLAvYvtipoA3G2uNNH5dvfkfd0r50sscVQKd3f_ZNkGBv3USU2eaa51", 0, v465);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfb947260, 0x00000000, 0, 0, 0x00000000, "", v465, __LINE__);
	return failed;
}

int variant140() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file159");
	FILE* v465 = fopen("file159", "w+t");
	failed = 0;
	fputs("[141/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1500 = fgets(v432, 64, v465);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x80e1f2b0, 0x00000000, 1, 0, 0x00000000, "", v465, __LINE__);
	return failed;
}

int variant141() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file159");
	FILE* v465 = fopen("file159", "w+t");
	failed = 0;
	fputs("[142/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1501 = fgets(v433, 64, v465);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1bde92b0, 0x00000000, 1, 0, 0x00000000, "", v465, __LINE__);
	return failed;
}

int variant142() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file159");
	FILE* v465 = fopen("file159", "w+t");
	failed = 0;
	fputs("[143/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1502 = fgets(v434, 64, v465);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc30942b0, 0x00000000, 1, 0, 0x00000000, "", v465, __LINE__);
	return failed;
}

int variant143() {
	int failed = 0;
	create_empty_file("empty159");
	FILE* v466 = fopen("empty159", "w+t");
	failed = 0;
	fputs("[144/432]fgets(NULL, Int(0), FILE* (Empty file, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1503 = fgets(NULL, 0, v466);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1a344260, 0x00000000, 0, 0, 0x00000000, "", v466, __LINE__);
	return failed;
}

int variant144() {
	int failed = 0;
	create_empty_file("empty159");
	FILE* v466 = fopen("empty159", "w+t");
	failed = 0;
	fputs("[145/432]fgets(Random text, Int(0), FILE* (Empty file, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1504 = fgets("UPawA3OH38GrGWP_J0Wt4dOHYSninV5FGbPDLGogJGCRsooO0MTEPlWXg_50uYG0zlpod64oXwoehei4NkdbrqDYf8Etw1ZEk1MY", 0, v466);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf3b14260, 0x00000000, 0, 0, 0x00000000, "", v466, __LINE__);
	return failed;
}

int variant145() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty159");
	FILE* v466 = fopen("empty159", "w+t");
	failed = 0;
	fputs("[146/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1505 = fgets(v432, 64, v466);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe211f2b0, 0x00000000, 1, 0, 0x00000000, "", v466, __LINE__);
	return failed;
}

int variant146() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty159");
	FILE* v466 = fopen("empty159", "w+t");
	failed = 0;
	fputs("[147/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1506 = fgets(v433, 64, v466);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x35a042b0, 0x00000000, 1, 0, 0x00000000, "", v466, __LINE__);
	return failed;
}

int variant147() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty159");
	FILE* v466 = fopen("empty159", "w+t");
	failed = 0;
	fputs("[148/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1507 = fgets(v434, 64, v466);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x059ab2b0, 0x00000000, 1, 0, 0x00000000, "", v466, __LINE__);
	return failed;
}

int variant148() {
	int failed = 0;
	create_random_file("random159");
	FILE* v467 = fopen("random159", "w+t");
	failed = 0;
	fputs("[149/432]fgets(NULL, Int(0), FILE* (File with random text, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1508 = fgets(NULL, 0, v467);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd2737260, 0x00000000, 0, 0, 0x00000000, "", v467, __LINE__);
	return failed;
}

int variant149() {
	int failed = 0;
	create_random_file("random159");
	FILE* v467 = fopen("random159", "w+t");
	failed = 0;
	fputs("[150/432]fgets(Random text, Int(0), FILE* (File with random text, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1509 = fgets("FNzKI02corL18xy4k5CSngfcPraH3msRRdALE_N1RFVhf79xajQzz7WbFL3dM_aq7d96Y2wVLnzaCoWJ1NyYkFj9iBlmE2MXbP7V", 0, v467);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x89536260, 0x00000000, 0, 0, 0x00000000, "", v467, __LINE__);
	return failed;
}

int variant150() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random159");
	FILE* v467 = fopen("random159", "w+t");
	failed = 0;
	fputs("[151/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1510 = fgets(v432, 64, v467);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8a3a32b0, 0x00000000, 1, 0, 0x00000000, "", v467, __LINE__);
	return failed;
}

int variant151() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random159");
	FILE* v467 = fopen("random159", "w+t");
	failed = 0;
	fputs("[152/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1511 = fgets(v433, 64, v467);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x826a42b0, 0x00000000, 1, 0, 0x00000000, "", v467, __LINE__);
	return failed;
}

int variant152() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random159");
	FILE* v467 = fopen("random159", "w+t");
	failed = 0;
	fputs("[153/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w+t\"))\n", stdout);
	errno = 0;
	char* v1512 = fgets(v434, 64, v467);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x155d12b0, 0x00000000, 1, 0, 0x00000000, "", v467, __LINE__);
	return failed;
}

int variant153() {
	int failed = 0;
	remove_file("file160");
	FILE* v468 = fopen("file160", "a+t");
	failed = 0;
	fputs("[154/432]fgets(NULL, Int(0), FILE* (File name, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1513 = fgets(NULL, 0, v468);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x85235260, 0x00000000, 0, 0, 0x00000000, "", v468, __LINE__);
	return failed;
}

int variant154() {
	int failed = 0;
	remove_file("file160");
	FILE* v468 = fopen("file160", "a+t");
	failed = 0;
	fputs("[155/432]fgets(Random text, Int(0), FILE* (File name, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1514 = fgets("ZW0CTlfLHgxtIxkIdFN_FkKcc1X6qQKoe_ud4yuEmmptwPpXo9Ef09cIhlSbyflS0nwwXmOd1GxUNvgB9ycxKvs5xLz2vCNtRYVg", 0, v468);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdb87e260, 0x00000000, 0, 0, 0x00000000, "", v468, __LINE__);
	return failed;
}

int variant155() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file160");
	FILE* v468 = fopen("file160", "a+t");
	failed = 0;
	fputs("[156/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1515 = fgets(v432, 64, v468);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe26512b0, 0x00000000, 1, 0, 0x00000000, "", v468, __LINE__);
	return failed;
}

int variant156() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file160");
	FILE* v468 = fopen("file160", "a+t");
	failed = 0;
	fputs("[157/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1516 = fgets(v433, 64, v468);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc292b2b0, 0x00000000, 1, 0, 0x00000000, "", v468, __LINE__);
	return failed;
}

int variant157() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file160");
	FILE* v468 = fopen("file160", "a+t");
	failed = 0;
	fputs("[158/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1517 = fgets(v434, 64, v468);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xcab432b0, 0x00000000, 1, 0, 0x00000000, "", v468, __LINE__);
	return failed;
}

int variant158() {
	int failed = 0;
	create_empty_file("empty160");
	FILE* v469 = fopen("empty160", "a+t");
	failed = 0;
	fputs("[159/432]fgets(NULL, Int(0), FILE* (Empty file, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1518 = fgets(NULL, 0, v469);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2444e260, 0x00000000, 0, 0, 0x00000000, "", v469, __LINE__);
	return failed;
}

int variant159() {
	int failed = 0;
	create_empty_file("empty160");
	FILE* v469 = fopen("empty160", "a+t");
	failed = 0;
	fputs("[160/432]fgets(Random text, Int(0), FILE* (Empty file, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1519 = fgets("ToXvC8EXmWO9OoS11WjvGBRak3TkNASASvKIfwDH8AOrBj75vyk2kQvEtNjlNd0WQ7OtVMxY7sZZ26_b3a5uKTWW7uxyQZxtypuL", 0, v469);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x188e5260, 0x00000000, 0, 0, 0x00000000, "", v469, __LINE__);
	return failed;
}

int variant160() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty160");
	FILE* v469 = fopen("empty160", "a+t");
	failed = 0;
	fputs("[161/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1520 = fgets(v432, 64, v469);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3f4662b0, 0x00000000, 1, 0, 0x00000000, "", v469, __LINE__);
	return failed;
}

int variant161() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty160");
	FILE* v469 = fopen("empty160", "a+t");
	failed = 0;
	fputs("[162/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1521 = fgets(v433, 64, v469);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3e1062b0, 0x00000000, 1, 0, 0x00000000, "", v469, __LINE__);
	return failed;
}

int variant162() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty160");
	FILE* v469 = fopen("empty160", "a+t");
	failed = 0;
	fputs("[163/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1522 = fgets(v434, 64, v469);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb0c362b0, 0x00000000, 1, 0, 0x00000000, "", v469, __LINE__);
	return failed;
}

int variant163() {
	int failed = 0;
	create_random_file("random160");
	FILE* v470 = fopen("random160", "a+t");
	failed = 0;
	fputs("[164/432]fgets(NULL, Int(0), FILE* (File with random text, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1523 = fgets(NULL, 0, v470);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc5b76260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v470, __LINE__);
	return failed;
}

int variant164() {
	int failed = 0;
	create_random_file("random160");
	FILE* v470 = fopen("random160", "a+t");
	failed = 0;
	fputs("[165/432]fgets(Random text, Int(0), FILE* (File with random text, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1524 = fgets("cwZVjvYR10jlMEbqcpkmTzbdxVtpuPYwselj62ee56Z7Iqclcf0rLOuYsFDRKO8WEhKO3ispeKGdnhlzXcFElejVghYaHs3yWIQG", 0, v470);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7a1f1260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v470, __LINE__);
	return failed;
}

int variant165() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random160");
	FILE* v470 = fopen("random160", "a+t");
	failed = 0;
	fputs("[166/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1525 = fgets(v432, 64, v470);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x372b62b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v470, __LINE__);
	return failed;
}

int variant166() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random160");
	FILE* v470 = fopen("random160", "a+t");
	failed = 0;
	fputs("[167/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1526 = fgets(v433, 64, v470);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe9aad2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v470, __LINE__);
	return failed;
}

int variant167() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random160");
	FILE* v470 = fopen("random160", "a+t");
	failed = 0;
	fputs("[168/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a+t\"))\n", stdout);
	errno = 0;
	char* v1527 = fgets(v434, 64, v470);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8b1952b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v470, __LINE__);
	return failed;
}

int variant168() {
	int failed = 0;
	remove_file("file161");
	FILE* v471 = fopen("file161", "rb");
	failed = 0;
	fputs("[169/432]fgets(NULL, Int(0), FILE* (File name, \"rb\"))\n", stdout);
	errno = 0;
	char* v1528 = fgets(NULL, 0, v471);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v471, __LINE__);
	return failed;
}

int variant169() {
	int failed = 0;
	remove_file("file161");
	FILE* v471 = fopen("file161", "rb");
	failed = 0;
	fputs("[170/432]fgets(Random text, Int(0), FILE* (File name, \"rb\"))\n", stdout);
	errno = 0;
	char* v1529 = fgets("003_7B5mr8ehgRwpfamwApKx1vifvC7jHwySRPRVL10M0_Ai6dHWg3RygaXdWI8UEZMFC5YgWmrK7PLvCTTecT_bjJcr7r5vi3AG", 0, v471);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v471, __LINE__);
	return failed;
}

int variant170() {
	int failed = 0;
	create_empty_file("empty161");
	FILE* v472 = fopen("empty161", "rb");
	failed = 0;
	fputs("[171/432]fgets(NULL, Int(0), FILE* (Empty file, \"rb\"))\n", stdout);
	errno = 0;
	char* v1533 = fgets(NULL, 0, v472);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xcc694260, 0x00000000, 0, 0, 0x00000000, "", v472, __LINE__);
	return failed;
}

int variant171() {
	int failed = 0;
	create_empty_file("empty161");
	FILE* v472 = fopen("empty161", "rb");
	failed = 0;
	fputs("[172/432]fgets(Random text, Int(0), FILE* (Empty file, \"rb\"))\n", stdout);
	errno = 0;
	char* v1534 = fgets("uCY_HZ_MXdUzZePr_KjW3vFdjSqPV5_vpiYJLR53w1WJFQU2PsrD8o4Rq1FJnXWrt5Hz7YvLPKEgQm0_IOKNW6ifcCaCHOXty7qN", 0, v472);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf808b260, 0x00000000, 0, 0, 0x00000000, "", v472, __LINE__);
	return failed;
}

int variant172() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty161");
	FILE* v472 = fopen("empty161", "rb");
	failed = 0;
	fputs("[173/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"rb\"))\n", stdout);
	errno = 0;
	char* v1535 = fgets(v432, 64, v472);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdbc702b0, 0x00000000, 1, 0, 0x00000000, "", v472, __LINE__);
	return failed;
}

int variant173() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty161");
	FILE* v472 = fopen("empty161", "rb");
	failed = 0;
	fputs("[174/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"rb\"))\n", stdout);
	errno = 0;
	char* v1536 = fgets(v433, 64, v472);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x07e4c2b0, 0x00000000, 1, 0, 0x00000000, "", v472, __LINE__);
	return failed;
}

int variant174() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty161");
	FILE* v472 = fopen("empty161", "rb");
	failed = 0;
	fputs("[175/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"rb\"))\n", stdout);
	errno = 0;
	char* v1537 = fgets(v434, 64, v472);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x991df2b0, 0x00000000, 1, 0, 0x00000000, "", v472, __LINE__);
	return failed;
}

int variant175() {
	int failed = 0;
	create_random_file("random161");
	FILE* v473 = fopen("random161", "rb");
	failed = 0;
	fputs("[176/432]fgets(NULL, Int(0), FILE* (File with random text, \"rb\"))\n", stdout);
	errno = 0;
	char* v1538 = fgets(NULL, 0, v473);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x783be260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v473, __LINE__);
	return failed;
}

int variant176() {
	int failed = 0;
	create_random_file("random161");
	FILE* v473 = fopen("random161", "rb");
	failed = 0;
	fputs("[177/432]fgets(Random text, Int(0), FILE* (File with random text, \"rb\"))\n", stdout);
	errno = 0;
	char* v1539 = fgets("CBuOQfeu8PDPQ3sys3TzZme6ILAKiq1kIk5_g4TcC7fcxrz93f3A43QFpu0xU1I0bEGVebSXAZcwSVY517jJxLl2eqPdK6_wwk6h", 0, v473);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x228ca260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v473, __LINE__);
	return failed;
}

int variant177() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random161");
	FILE* v473 = fopen("random161", "rb");
	failed = 0;
	fputs("[178/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"rb\"))\n", stdout);
	errno = 0;
	char* v1540 = fgets(v432, 64, v473);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xba7092b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v473, __LINE__);
	return failed;
}

int variant178() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random161");
	FILE* v473 = fopen("random161", "rb");
	failed = 0;
	fputs("[179/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"rb\"))\n", stdout);
	errno = 0;
	char* v1541 = fgets(v433, 64, v473);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x42d0c2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v473, __LINE__);
	return failed;
}

int variant179() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random161");
	FILE* v473 = fopen("random161", "rb");
	failed = 0;
	fputs("[180/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"rb\"))\n", stdout);
	errno = 0;
	char* v1542 = fgets(v434, 64, v473);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7c8242b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v473, __LINE__);
	return failed;
}

int variant180() {
	int failed = 0;
	remove_file("file162");
	FILE* v474 = fopen("file162", "wb");
	failed = 0;
	fputs("[181/432]fgets(NULL, Int(0), FILE* (File name, \"wb\"))\n", stdout);
	errno = 0;
	char* v1543 = fgets(NULL, 0, v474);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3fa69260, 0x00000000, 0, 0, 0x00000000, "", v474, __LINE__);
	return failed;
}

int variant181() {
	int failed = 0;
	remove_file("file162");
	FILE* v474 = fopen("file162", "wb");
	failed = 0;
	fputs("[182/432]fgets(Random text, Int(0), FILE* (File name, \"wb\"))\n", stdout);
	errno = 0;
	char* v1544 = fgets("Xwvgy1sfZ2qr6lHk_f9wLQ54atGbJF2upP0IVWlR6uJYAOvIrn5hIkhPzcUu515ZF4LuoNfnEvzBTOusdqkoCxWtuiGkQi9e9OJo", 0, v474);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x88ce7260, 0x00000000, 0, 0, 0x00000000, "", v474, __LINE__);
	return failed;
}

int variant182() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file162");
	FILE* v474 = fopen("file162", "wb");
	failed = 0;
	fputs("[183/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"wb\"))\n", stdout);
	errno = 0;
	char* v1545 = fgets(v432, 64, v474);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa4ae62b0, 0x00000000, 0, 1, 0x00000000, "", v474, __LINE__);
	return failed;
}

int variant183() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file162");
	FILE* v474 = fopen("file162", "wb");
	failed = 0;
	fputs("[184/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"wb\"))\n", stdout);
	errno = 0;
	char* v1546 = fgets(v433, 64, v474);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x97ffd2b0, 0x00000000, 0, 1, 0x00000000, "", v474, __LINE__);
	return failed;
}

int variant184() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file162");
	FILE* v474 = fopen("file162", "wb");
	failed = 0;
	fputs("[185/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"wb\"))\n", stdout);
	errno = 0;
	char* v1547 = fgets(v434, 64, v474);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe6d1c2b0, 0x00000000, 0, 1, 0x00000000, "", v474, __LINE__);
	return failed;
}

int variant185() {
	int failed = 0;
	create_empty_file("empty162");
	FILE* v475 = fopen("empty162", "wb");
	failed = 0;
	fputs("[186/432]fgets(NULL, Int(0), FILE* (Empty file, \"wb\"))\n", stdout);
	errno = 0;
	char* v1548 = fgets(NULL, 0, v475);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x17233260, 0x00000000, 0, 0, 0x00000000, "", v475, __LINE__);
	return failed;
}

int variant186() {
	int failed = 0;
	create_empty_file("empty162");
	FILE* v475 = fopen("empty162", "wb");
	failed = 0;
	fputs("[187/432]fgets(Random text, Int(0), FILE* (Empty file, \"wb\"))\n", stdout);
	errno = 0;
	char* v1549 = fgets("qQFkPVh7wnGcz_GFnH25GdwRTfiSiCEfKr9zhtY5HnQJsM2RhrfzSaGMVX5kbsqDYsroCw7DRb5arBU4_54BxtDq8VrPODahYdpf", 0, v475);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x45593260, 0x00000000, 0, 0, 0x00000000, "", v475, __LINE__);
	return failed;
}

int variant187() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty162");
	FILE* v475 = fopen("empty162", "wb");
	failed = 0;
	fputs("[188/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"wb\"))\n", stdout);
	errno = 0;
	char* v1550 = fgets(v432, 64, v475);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x327712b0, 0x00000000, 0, 1, 0x00000000, "", v475, __LINE__);
	return failed;
}

int variant188() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty162");
	FILE* v475 = fopen("empty162", "wb");
	failed = 0;
	fputs("[189/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"wb\"))\n", stdout);
	errno = 0;
	char* v1551 = fgets(v433, 64, v475);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4aa602b0, 0x00000000, 0, 1, 0x00000000, "", v475, __LINE__);
	return failed;
}

int variant189() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty162");
	FILE* v475 = fopen("empty162", "wb");
	failed = 0;
	fputs("[190/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"wb\"))\n", stdout);
	errno = 0;
	char* v1552 = fgets(v434, 64, v475);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7de3f2b0, 0x00000000, 0, 1, 0x00000000, "", v475, __LINE__);
	return failed;
}

int variant190() {
	int failed = 0;
	create_random_file("random162");
	FILE* v476 = fopen("random162", "wb");
	failed = 0;
	fputs("[191/432]fgets(NULL, Int(0), FILE* (File with random text, \"wb\"))\n", stdout);
	errno = 0;
	char* v1553 = fgets(NULL, 0, v476);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3ed86260, 0x00000000, 0, 0, 0x00000000, "", v476, __LINE__);
	return failed;
}

int variant191() {
	int failed = 0;
	create_random_file("random162");
	FILE* v476 = fopen("random162", "wb");
	failed = 0;
	fputs("[192/432]fgets(Random text, Int(0), FILE* (File with random text, \"wb\"))\n", stdout);
	errno = 0;
	char* v1554 = fgets("phcE9CIUZhxzqNMjw8THn23o5ofNOQLK0aj7ZUwK9Ac4HMG0gVUozia0sRed_AHsIemQ37A_ygeIhfZPaKgksWKqS3Fi2guP5USh", 0, v476);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa96fd260, 0x00000000, 0, 0, 0x00000000, "", v476, __LINE__);
	return failed;
}

int variant192() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random162");
	FILE* v476 = fopen("random162", "wb");
	failed = 0;
	fputs("[193/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"wb\"))\n", stdout);
	errno = 0;
	char* v1555 = fgets(v432, 64, v476);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x763ec2b0, 0x00000000, 0, 1, 0x00000000, "", v476, __LINE__);
	return failed;
}

int variant193() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random162");
	FILE* v476 = fopen("random162", "wb");
	failed = 0;
	fputs("[194/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"wb\"))\n", stdout);
	errno = 0;
	char* v1556 = fgets(v433, 64, v476);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1203b2b0, 0x00000000, 0, 1, 0x00000000, "", v476, __LINE__);
	return failed;
}

int variant194() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random162");
	FILE* v476 = fopen("random162", "wb");
	failed = 0;
	fputs("[195/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"wb\"))\n", stdout);
	errno = 0;
	char* v1557 = fgets(v434, 64, v476);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x607472b0, 0x00000000, 0, 1, 0x00000000, "", v476, __LINE__);
	return failed;
}

int variant195() {
	int failed = 0;
	remove_file("file163");
	FILE* v477 = fopen("file163", "ab");
	failed = 0;
	fputs("[196/432]fgets(NULL, Int(0), FILE* (File name, \"ab\"))\n", stdout);
	errno = 0;
	char* v1558 = fgets(NULL, 0, v477);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2501a260, 0x00000000, 0, 0, 0x00000000, "", v477, __LINE__);
	return failed;
}

int variant196() {
	int failed = 0;
	remove_file("file163");
	FILE* v477 = fopen("file163", "ab");
	failed = 0;
	fputs("[197/432]fgets(Random text, Int(0), FILE* (File name, \"ab\"))\n", stdout);
	errno = 0;
	char* v1559 = fgets("ZXYnrd5w0pgQLecGYuinRoYwhQzoaYMWxdZ1M3yY_8bw8jruoD7EGXAo6FehfZccUj1cfyPHh3lK2C6uMBP_t9VLucIlPkgDJqG2", 0, v477);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6cb23260, 0x00000000, 0, 0, 0x00000000, "", v477, __LINE__);
	return failed;
}

int variant197() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file163");
	FILE* v477 = fopen("file163", "ab");
	failed = 0;
	fputs("[198/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"ab\"))\n", stdout);
	errno = 0;
	char* v1560 = fgets(v432, 64, v477);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x276362b0, 0x00000000, 0, 1, 0x00000000, "", v477, __LINE__);
	return failed;
}

int variant198() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file163");
	FILE* v477 = fopen("file163", "ab");
	failed = 0;
	fputs("[199/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"ab\"))\n", stdout);
	errno = 0;
	char* v1561 = fgets(v433, 64, v477);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3b1f32b0, 0x00000000, 0, 1, 0x00000000, "", v477, __LINE__);
	return failed;
}

int variant199() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file163");
	FILE* v477 = fopen("file163", "ab");
	failed = 0;
	fputs("[200/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"ab\"))\n", stdout);
	errno = 0;
	char* v1562 = fgets(v434, 64, v477);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x31c8f2b0, 0x00000000, 0, 1, 0x00000000, "", v477, __LINE__);
	return failed;
}

int variant200() {
	int failed = 0;
	create_empty_file("empty163");
	FILE* v478 = fopen("empty163", "ab");
	failed = 0;
	fputs("[201/432]fgets(NULL, Int(0), FILE* (Empty file, \"ab\"))\n", stdout);
	errno = 0;
	char* v1563 = fgets(NULL, 0, v478);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6c246260, 0x00000000, 0, 0, 0x00000000, "", v478, __LINE__);
	return failed;
}

int variant201() {
	int failed = 0;
	create_empty_file("empty163");
	FILE* v478 = fopen("empty163", "ab");
	failed = 0;
	fputs("[202/432]fgets(Random text, Int(0), FILE* (Empty file, \"ab\"))\n", stdout);
	errno = 0;
	char* v1564 = fgets("aMpbhDq8TK8_XQwhzL2ScZePzY_54dSH30apw5sv_xvuFePB0eNUHAU8_E1zmFhG0ZgN51Qyh9qOd8QFAr3lSPXj9P9DoRWedoaC", 0, v478);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x55f2a260, 0x00000000, 0, 0, 0x00000000, "", v478, __LINE__);
	return failed;
}

int variant202() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty163");
	FILE* v478 = fopen("empty163", "ab");
	failed = 0;
	fputs("[203/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"ab\"))\n", stdout);
	errno = 0;
	char* v1565 = fgets(v432, 64, v478);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x469b22b0, 0x00000000, 0, 1, 0x00000000, "", v478, __LINE__);
	return failed;
}

int variant203() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty163");
	FILE* v478 = fopen("empty163", "ab");
	failed = 0;
	fputs("[204/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"ab\"))\n", stdout);
	errno = 0;
	char* v1566 = fgets(v433, 64, v478);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x70e5e2b0, 0x00000000, 0, 1, 0x00000000, "", v478, __LINE__);
	return failed;
}

int variant204() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty163");
	FILE* v478 = fopen("empty163", "ab");
	failed = 0;
	fputs("[205/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"ab\"))\n", stdout);
	errno = 0;
	char* v1567 = fgets(v434, 64, v478);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xecb632b0, 0x00000000, 0, 1, 0x00000000, "", v478, __LINE__);
	return failed;
}

int variant205() {
	int failed = 0;
	create_random_file("random163");
	FILE* v479 = fopen("random163", "ab");
	failed = 0;
	fputs("[206/432]fgets(NULL, Int(0), FILE* (File with random text, \"ab\"))\n", stdout);
	errno = 0;
	char* v1568 = fgets(NULL, 0, v479);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3b997260, 0x00000400, 0, 0, 0x00000400, "", v479, __LINE__);
	return failed;
}

int variant206() {
	int failed = 0;
	create_random_file("random163");
	FILE* v479 = fopen("random163", "ab");
	failed = 0;
	fputs("[207/432]fgets(Random text, Int(0), FILE* (File with random text, \"ab\"))\n", stdout);
	errno = 0;
	char* v1569 = fgets("xMRwSjas6SAXith4yuRshuT8q6fjPkKdoPKRqw46BsRAPqONkj0fVU5Kr_RpjbzFdP23g16x9LL8qVSlLNG4O1RNIRd_9cNJ6U2D", 0, v479);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x73746260, 0x00000400, 0, 0, 0x00000400, "", v479, __LINE__);
	return failed;
}

int variant207() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random163");
	FILE* v479 = fopen("random163", "ab");
	failed = 0;
	fputs("[208/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"ab\"))\n", stdout);
	errno = 0;
	char* v1570 = fgets(v432, 64, v479);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x38f462b0, 0x00000400, 0, 1, 0x00000400, "", v479, __LINE__);
	return failed;
}

int variant208() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random163");
	FILE* v479 = fopen("random163", "ab");
	failed = 0;
	fputs("[209/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"ab\"))\n", stdout);
	errno = 0;
	char* v1571 = fgets(v433, 64, v479);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa69222b0, 0x00000400, 0, 1, 0x00000400, "", v479, __LINE__);
	return failed;
}

int variant209() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random163");
	FILE* v479 = fopen("random163", "ab");
	failed = 0;
	fputs("[210/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"ab\"))\n", stdout);
	errno = 0;
	char* v1572 = fgets(v434, 64, v479);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0a9702b0, 0x00000400, 0, 1, 0x00000400, "", v479, __LINE__);
	return failed;
}

int variant210() {
	int failed = 0;
	remove_file("file164");
	FILE* v480 = fopen("file164", "r+b");
	failed = 0;
	fputs("[211/432]fgets(NULL, Int(0), FILE* (File name, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1573 = fgets(NULL, 0, v480);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v480, __LINE__);
	return failed;
}

int variant211() {
	int failed = 0;
	remove_file("file164");
	FILE* v480 = fopen("file164", "r+b");
	failed = 0;
	fputs("[212/432]fgets(Random text, Int(0), FILE* (File name, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1574 = fgets("HtKta2J9IICip_ct7iak_MIyNVRhvEy9SfVP_gmZDEQseG_JH5uncYt81NqQD4HAWEiSFxNQAEEfdoktt3WFf6h3dgi7NTLWgnKv", 0, v480);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v480, __LINE__);
	return failed;
}

int variant212() {
	int failed = 0;
	create_empty_file("empty164");
	FILE* v481 = fopen("empty164", "r+b");
	failed = 0;
	fputs("[213/432]fgets(NULL, Int(0), FILE* (Empty file, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1578 = fgets(NULL, 0, v481);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb076b260, 0x00000000, 0, 0, 0x00000000, "", v481, __LINE__);
	return failed;
}

int variant213() {
	int failed = 0;
	create_empty_file("empty164");
	FILE* v481 = fopen("empty164", "r+b");
	failed = 0;
	fputs("[214/432]fgets(Random text, Int(0), FILE* (Empty file, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1579 = fgets("UpjeCn5wjIUfk4O2_lg2hVxWCiY5iFQ0JHSLsr4T2HugQorA7jLqgcUvWHAltZ32ZOihGQOU9pwsHPgiajT6qTEqf157fGquHc0o", 0, v481);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x89438260, 0x00000000, 0, 0, 0x00000000, "", v481, __LINE__);
	return failed;
}

int variant214() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty164");
	FILE* v481 = fopen("empty164", "r+b");
	failed = 0;
	fputs("[215/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1580 = fgets(v432, 64, v481);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbedc12b0, 0x00000000, 1, 0, 0x00000000, "", v481, __LINE__);
	return failed;
}

int variant215() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty164");
	FILE* v481 = fopen("empty164", "r+b");
	failed = 0;
	fputs("[216/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1581 = fgets(v433, 64, v481);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf2a3d2b0, 0x00000000, 1, 0, 0x00000000, "", v481, __LINE__);
	return failed;
}

int variant216() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty164");
	FILE* v481 = fopen("empty164", "r+b");
	failed = 0;
	fputs("[217/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1582 = fgets(v434, 64, v481);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x03d9f2b0, 0x00000000, 1, 0, 0x00000000, "", v481, __LINE__);
	return failed;
}

int variant217() {
	int failed = 0;
	create_random_file("random164");
	FILE* v482 = fopen("random164", "r+b");
	failed = 0;
	fputs("[218/432]fgets(NULL, Int(0), FILE* (File with random text, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1583 = fgets(NULL, 0, v482);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfea99260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v482, __LINE__);
	return failed;
}

int variant218() {
	int failed = 0;
	create_random_file("random164");
	FILE* v482 = fopen("random164", "r+b");
	failed = 0;
	fputs("[219/432]fgets(Random text, Int(0), FILE* (File with random text, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1584 = fgets("BgEnUlRFOYUiHghtg8PsLJ1aYsBh0s32KE_LT24Cc7eivRLil98Z5NuN3sMdFGHwXC7ypPbmTRspNOfkp2srgo_Ak4t_grVa74s2", 0, v482);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x20e02260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v482, __LINE__);
	return failed;
}

int variant219() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random164");
	FILE* v482 = fopen("random164", "r+b");
	failed = 0;
	fputs("[220/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1585 = fgets(v432, 64, v482);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x790d82b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v482, __LINE__);
	return failed;
}

int variant220() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random164");
	FILE* v482 = fopen("random164", "r+b");
	failed = 0;
	fputs("[221/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1586 = fgets(v433, 64, v482);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc21e52b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v482, __LINE__);
	return failed;
}

int variant221() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random164");
	FILE* v482 = fopen("random164", "r+b");
	failed = 0;
	fputs("[222/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r+b\"))\n", stdout);
	errno = 0;
	char* v1587 = fgets(v434, 64, v482);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2c07a2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v482, __LINE__);
	return failed;
}

int variant222() {
	int failed = 0;
	remove_file("file165");
	FILE* v483 = fopen("file165", "w+b");
	failed = 0;
	fputs("[223/432]fgets(NULL, Int(0), FILE* (File name, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1588 = fgets(NULL, 0, v483);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xde205260, 0x00000000, 0, 0, 0x00000000, "", v483, __LINE__);
	return failed;
}

int variant223() {
	int failed = 0;
	remove_file("file165");
	FILE* v483 = fopen("file165", "w+b");
	failed = 0;
	fputs("[224/432]fgets(Random text, Int(0), FILE* (File name, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1589 = fgets("JLxlXlTs8o30Bil7ysTUGG_rsRAOB3MQnc0wgEWaxdomwL2IOJOUYTuc3WfLwwSypi1XUDKzg7ebOj9thUOlhBMKGZSRmAaOOMQ_", 0, v483);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe73f4260, 0x00000000, 0, 0, 0x00000000, "", v483, __LINE__);
	return failed;
}

int variant224() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file165");
	FILE* v483 = fopen("file165", "w+b");
	failed = 0;
	fputs("[225/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1590 = fgets(v432, 64, v483);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdde102b0, 0x00000000, 1, 0, 0x00000000, "", v483, __LINE__);
	return failed;
}

int variant225() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file165");
	FILE* v483 = fopen("file165", "w+b");
	failed = 0;
	fputs("[226/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1591 = fgets(v433, 64, v483);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb3c6e2b0, 0x00000000, 1, 0, 0x00000000, "", v483, __LINE__);
	return failed;
}

int variant226() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file165");
	FILE* v483 = fopen("file165", "w+b");
	failed = 0;
	fputs("[227/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1592 = fgets(v434, 64, v483);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0f9262b0, 0x00000000, 1, 0, 0x00000000, "", v483, __LINE__);
	return failed;
}

int variant227() {
	int failed = 0;
	create_empty_file("empty165");
	FILE* v484 = fopen("empty165", "w+b");
	failed = 0;
	fputs("[228/432]fgets(NULL, Int(0), FILE* (Empty file, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1593 = fgets(NULL, 0, v484);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7caa5260, 0x00000000, 0, 0, 0x00000000, "", v484, __LINE__);
	return failed;
}

int variant228() {
	int failed = 0;
	create_empty_file("empty165");
	FILE* v484 = fopen("empty165", "w+b");
	failed = 0;
	fputs("[229/432]fgets(Random text, Int(0), FILE* (Empty file, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1594 = fgets("T41Z4KB1mnGnfz89uChkKKBu2mj9TQpbNIoCuEdQrQxZ8Ob07MYx499hZrKASB8G80t7psg0JR5piatBuVBto1d9skfv2j3dSvwS", 0, v484);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x89fc9260, 0x00000000, 0, 0, 0x00000000, "", v484, __LINE__);
	return failed;
}

int variant229() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty165");
	FILE* v484 = fopen("empty165", "w+b");
	failed = 0;
	fputs("[230/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1595 = fgets(v432, 64, v484);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd8a6f2b0, 0x00000000, 1, 0, 0x00000000, "", v484, __LINE__);
	return failed;
}

int variant230() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty165");
	FILE* v484 = fopen("empty165", "w+b");
	failed = 0;
	fputs("[231/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1596 = fgets(v433, 64, v484);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe6eb72b0, 0x00000000, 1, 0, 0x00000000, "", v484, __LINE__);
	return failed;
}

int variant231() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty165");
	FILE* v484 = fopen("empty165", "w+b");
	failed = 0;
	fputs("[232/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1597 = fgets(v434, 64, v484);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x052ee2b0, 0x00000000, 1, 0, 0x00000000, "", v484, __LINE__);
	return failed;
}

int variant232() {
	int failed = 0;
	create_random_file("random165");
	FILE* v485 = fopen("random165", "w+b");
	failed = 0;
	fputs("[233/432]fgets(NULL, Int(0), FILE* (File with random text, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1598 = fgets(NULL, 0, v485);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf0979260, 0x00000000, 0, 0, 0x00000000, "", v485, __LINE__);
	return failed;
}

int variant233() {
	int failed = 0;
	create_random_file("random165");
	FILE* v485 = fopen("random165", "w+b");
	failed = 0;
	fputs("[234/432]fgets(Random text, Int(0), FILE* (File with random text, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1599 = fgets("SDfaSNrhxox1eGqOW7W3dBMSGAaCWVr37qrxFKJhyj5mOjEh2LHXq51kI7j2gTPyMdTP0MZLkEG1MtN6hCzk3donSu2tGiorthEj", 0, v485);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2a8aa260, 0x00000000, 0, 0, 0x00000000, "", v485, __LINE__);
	return failed;
}

int variant234() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random165");
	FILE* v485 = fopen("random165", "w+b");
	failed = 0;
	fputs("[235/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1600 = fgets(v432, 64, v485);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1fa3a2b0, 0x00000000, 1, 0, 0x00000000, "", v485, __LINE__);
	return failed;
}

int variant235() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random165");
	FILE* v485 = fopen("random165", "w+b");
	failed = 0;
	fputs("[236/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1601 = fgets(v433, 64, v485);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x458ac2b0, 0x00000000, 1, 0, 0x00000000, "", v485, __LINE__);
	return failed;
}

int variant236() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random165");
	FILE* v485 = fopen("random165", "w+b");
	failed = 0;
	fputs("[237/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w+b\"))\n", stdout);
	errno = 0;
	char* v1602 = fgets(v434, 64, v485);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6f7de2b0, 0x00000000, 1, 0, 0x00000000, "", v485, __LINE__);
	return failed;
}

int variant237() {
	int failed = 0;
	remove_file("file166");
	FILE* v486 = fopen("file166", "a+b");
	failed = 0;
	fputs("[238/432]fgets(NULL, Int(0), FILE* (File name, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1603 = fgets(NULL, 0, v486);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7a385260, 0x00000000, 0, 0, 0x00000000, "", v486, __LINE__);
	return failed;
}

int variant238() {
	int failed = 0;
	remove_file("file166");
	FILE* v486 = fopen("file166", "a+b");
	failed = 0;
	fputs("[239/432]fgets(Random text, Int(0), FILE* (File name, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1604 = fgets("y8O25EhhgYDhOgk6ytReI3fpt0ZmzrRK55QJyuUuiNtzVNII0O5947C7Tl002qocHc4fXA8VD2JS_QsCKJx0nG3DLcucxWkTfrOz", 0, v486);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1c140260, 0x00000000, 0, 0, 0x00000000, "", v486, __LINE__);
	return failed;
}

int variant239() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file166");
	FILE* v486 = fopen("file166", "a+b");
	failed = 0;
	fputs("[240/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1605 = fgets(v432, 64, v486);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd1c632b0, 0x00000000, 1, 0, 0x00000000, "", v486, __LINE__);
	return failed;
}

int variant240() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file166");
	FILE* v486 = fopen("file166", "a+b");
	failed = 0;
	fputs("[241/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1606 = fgets(v433, 64, v486);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6cf8f2b0, 0x00000000, 1, 0, 0x00000000, "", v486, __LINE__);
	return failed;
}

int variant241() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file166");
	FILE* v486 = fopen("file166", "a+b");
	failed = 0;
	fputs("[242/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1607 = fgets(v434, 64, v486);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x265b22b0, 0x00000000, 1, 0, 0x00000000, "", v486, __LINE__);
	return failed;
}

int variant242() {
	int failed = 0;
	create_empty_file("empty166");
	FILE* v487 = fopen("empty166", "a+b");
	failed = 0;
	fputs("[243/432]fgets(NULL, Int(0), FILE* (Empty file, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1608 = fgets(NULL, 0, v487);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x85134260, 0x00000000, 0, 0, 0x00000000, "", v487, __LINE__);
	return failed;
}

int variant243() {
	int failed = 0;
	create_empty_file("empty166");
	FILE* v487 = fopen("empty166", "a+b");
	failed = 0;
	fputs("[244/432]fgets(Random text, Int(0), FILE* (Empty file, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1609 = fgets("RBOC7yQC7hLrjs3TNx78dQNQQp54OSLJL4CyPaCh1E_UO9ptKb2RHqf8BTpgSZtF6hmUHvXC0fCsehgc6Ax81XP8XYYcowgdsMQF", 0, v487);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9d66e260, 0x00000000, 0, 0, 0x00000000, "", v487, __LINE__);
	return failed;
}

int variant244() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty166");
	FILE* v487 = fopen("empty166", "a+b");
	failed = 0;
	fputs("[245/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1610 = fgets(v432, 64, v487);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0db3f2b0, 0x00000000, 1, 0, 0x00000000, "", v487, __LINE__);
	return failed;
}

int variant245() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty166");
	FILE* v487 = fopen("empty166", "a+b");
	failed = 0;
	fputs("[246/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1611 = fgets(v433, 64, v487);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7bac32b0, 0x00000000, 1, 0, 0x00000000, "", v487, __LINE__);
	return failed;
}

int variant246() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty166");
	FILE* v487 = fopen("empty166", "a+b");
	failed = 0;
	fputs("[247/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1612 = fgets(v434, 64, v487);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x52fcd2b0, 0x00000000, 1, 0, 0x00000000, "", v487, __LINE__);
	return failed;
}

int variant247() {
	int failed = 0;
	create_random_file("random166");
	FILE* v488 = fopen("random166", "a+b");
	failed = 0;
	fputs("[248/432]fgets(NULL, Int(0), FILE* (File with random text, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1613 = fgets(NULL, 0, v488);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4184d260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v488, __LINE__);
	return failed;
}

int variant248() {
	int failed = 0;
	create_random_file("random166");
	FILE* v488 = fopen("random166", "a+b");
	failed = 0;
	fputs("[249/432]fgets(Random text, Int(0), FILE* (File with random text, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1614 = fgets("4x5OeiMgzd0A4dMfEtIUN2xMhysBu9nkoqNCuq4FXxvjqvkc3iTwnVqu0a3b08ogrdUFuqD5pEeO66hXPjghtLJy7i9KyjbqgyW0", 0, v488);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe0fce260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v488, __LINE__);
	return failed;
}

int variant249() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random166");
	FILE* v488 = fopen("random166", "a+b");
	failed = 0;
	fputs("[250/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1615 = fgets(v432, 64, v488);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe81592b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v488, __LINE__);
	return failed;
}

int variant250() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random166");
	FILE* v488 = fopen("random166", "a+b");
	failed = 0;
	fputs("[251/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1616 = fgets(v433, 64, v488);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3a1542b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v488, __LINE__);
	return failed;
}

int variant251() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random166");
	FILE* v488 = fopen("random166", "a+b");
	failed = 0;
	fputs("[252/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a+b\"))\n", stdout);
	errno = 0;
	char* v1617 = fgets(v434, 64, v488);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xba37c2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v488, __LINE__);
	return failed;
}

int variant252() {
	int failed = 0;
	remove_file("file167");
	FILE* v489 = fopen("file167", "rx");
	failed = 0;
	fputs("[253/432]fgets(NULL, Int(0), FILE* (File name, \"rx\"))\n", stdout);
	errno = 0;
	char* v1618 = fgets(NULL, 0, v489);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v489, __LINE__);
	return failed;
}

int variant253() {
	int failed = 0;
	remove_file("file167");
	FILE* v489 = fopen("file167", "rx");
	failed = 0;
	fputs("[254/432]fgets(Random text, Int(0), FILE* (File name, \"rx\"))\n", stdout);
	errno = 0;
	char* v1619 = fgets("yBqCdBGLOPua6DcxgBtn8Nk4wqd6nGuKq9aRLU2S4BGJ9UW6G3ckPz0aAUD55YqmDDpNvmcQnvaRHwrEbNpkKnDulSSMNL7mOM0O", 0, v489);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v489, __LINE__);
	return failed;
}

int variant254() {
	int failed = 0;
	create_empty_file("empty167");
	FILE* v490 = fopen("empty167", "rx");
	failed = 0;
	fputs("[255/432]fgets(NULL, Int(0), FILE* (Empty file, \"rx\"))\n", stdout);
	errno = 0;
	char* v1623 = fgets(NULL, 0, v490);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa6201260, 0x00000000, 0, 0, 0x00000000, "", v490, __LINE__);
	return failed;
}

int variant255() {
	int failed = 0;
	create_empty_file("empty167");
	FILE* v490 = fopen("empty167", "rx");
	failed = 0;
	fputs("[256/432]fgets(Random text, Int(0), FILE* (Empty file, \"rx\"))\n", stdout);
	errno = 0;
	char* v1624 = fgets("XirvNEDvpvByKTb0jGWtRbXzHXEELxgOfFa8EBOwo0YUNjoDxgrhFTySt9esImT6Yqi6WeYnm0Poj3_3rGFtJxjwTIqOAn7m5948", 0, v490);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb06c8260, 0x00000000, 0, 0, 0x00000000, "", v490, __LINE__);
	return failed;
}

int variant256() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty167");
	FILE* v490 = fopen("empty167", "rx");
	failed = 0;
	fputs("[257/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"rx\"))\n", stdout);
	errno = 0;
	char* v1625 = fgets(v432, 64, v490);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x152622b0, 0x00000000, 1, 0, 0x00000000, "", v490, __LINE__);
	return failed;
}

int variant257() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty167");
	FILE* v490 = fopen("empty167", "rx");
	failed = 0;
	fputs("[258/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"rx\"))\n", stdout);
	errno = 0;
	char* v1626 = fgets(v433, 64, v490);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0c31e2b0, 0x00000000, 1, 0, 0x00000000, "", v490, __LINE__);
	return failed;
}

int variant258() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty167");
	FILE* v490 = fopen("empty167", "rx");
	failed = 0;
	fputs("[259/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"rx\"))\n", stdout);
	errno = 0;
	char* v1627 = fgets(v434, 64, v490);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe39622b0, 0x00000000, 1, 0, 0x00000000, "", v490, __LINE__);
	return failed;
}

int variant259() {
	int failed = 0;
	create_random_file("random167");
	FILE* v491 = fopen("random167", "rx");
	failed = 0;
	fputs("[260/432]fgets(NULL, Int(0), FILE* (File with random text, \"rx\"))\n", stdout);
	errno = 0;
	char* v1628 = fgets(NULL, 0, v491);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa04ca260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v491, __LINE__);
	return failed;
}

int variant260() {
	int failed = 0;
	create_random_file("random167");
	FILE* v491 = fopen("random167", "rx");
	failed = 0;
	fputs("[261/432]fgets(Random text, Int(0), FILE* (File with random text, \"rx\"))\n", stdout);
	errno = 0;
	char* v1629 = fgets("0mBRF1j4Wh6WiBTesEtMFLv4I28NbhPOMAeEOLKiJxuMV51rnKLhsqKcffVuv4_r9BWGvA1G8cqBnQqzN613tmVy9feVwkyk22J8", 0, v491);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0f758260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v491, __LINE__);
	return failed;
}

int variant261() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random167");
	FILE* v491 = fopen("random167", "rx");
	failed = 0;
	fputs("[262/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"rx\"))\n", stdout);
	errno = 0;
	char* v1630 = fgets(v432, 64, v491);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x0f8fb2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v491, __LINE__);
	return failed;
}

int variant262() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random167");
	FILE* v491 = fopen("random167", "rx");
	failed = 0;
	fputs("[263/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"rx\"))\n", stdout);
	errno = 0;
	char* v1631 = fgets(v433, 64, v491);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xf9a432b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v491, __LINE__);
	return failed;
}

int variant263() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random167");
	FILE* v491 = fopen("random167", "rx");
	failed = 0;
	fputs("[264/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"rx\"))\n", stdout);
	errno = 0;
	char* v1632 = fgets(v434, 64, v491);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xecce92b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v491, __LINE__);
	return failed;
}

int variant264() {
	int failed = 0;
	remove_file("file168");
	FILE* v492 = fopen("file168", "wx");
	failed = 0;
	fputs("[265/432]fgets(NULL, Int(0), FILE* (File name, \"wx\"))\n", stdout);
	errno = 0;
	char* v1633 = fgets(NULL, 0, v492);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1ae72260, 0x00000000, 0, 0, 0x00000000, "", v492, __LINE__);
	return failed;
}

int variant265() {
	int failed = 0;
	remove_file("file168");
	FILE* v492 = fopen("file168", "wx");
	failed = 0;
	fputs("[266/432]fgets(Random text, Int(0), FILE* (File name, \"wx\"))\n", stdout);
	errno = 0;
	char* v1634 = fgets("BnWwjEIeJVcFXF3Hivt0kkdSVZLsasL_bLUPwKTzTrb0U0akdb7CxtY6yZ4SDVNev3MXbBugsaTvHk1nhB2aVSMGyC0xn_zdnv8X", 0, v492);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8b552260, 0x00000000, 0, 0, 0x00000000, "", v492, __LINE__);
	return failed;
}

int variant266() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file168");
	FILE* v492 = fopen("file168", "wx");
	failed = 0;
	fputs("[267/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"wx\"))\n", stdout);
	errno = 0;
	char* v1635 = fgets(v432, 64, v492);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x338e32b0, 0x00000000, 0, 1, 0x00000000, "", v492, __LINE__);
	return failed;
}

int variant267() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file168");
	FILE* v492 = fopen("file168", "wx");
	failed = 0;
	fputs("[268/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"wx\"))\n", stdout);
	errno = 0;
	char* v1636 = fgets(v433, 64, v492);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x4f63f2b0, 0x00000000, 0, 1, 0x00000000, "", v492, __LINE__);
	return failed;
}

int variant268() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file168");
	FILE* v492 = fopen("file168", "wx");
	failed = 0;
	fputs("[269/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"wx\"))\n", stdout);
	errno = 0;
	char* v1637 = fgets(v434, 64, v492);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa9f452b0, 0x00000000, 0, 1, 0x00000000, "", v492, __LINE__);
	return failed;
}

int variant269() {
	int failed = 0;
	create_empty_file("empty168");
	FILE* v493 = fopen("empty168", "wx");
	failed = 0;
	fputs("[270/432]fgets(NULL, Int(0), FILE* (Empty file, \"wx\"))\n", stdout);
	errno = 0;
	char* v1638 = fgets(NULL, 0, v493);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v493, __LINE__);
	return failed;
}

int variant270() {
	int failed = 0;
	create_empty_file("empty168");
	FILE* v493 = fopen("empty168", "wx");
	failed = 0;
	fputs("[271/432]fgets(Random text, Int(0), FILE* (Empty file, \"wx\"))\n", stdout);
	errno = 0;
	char* v1639 = fgets("L__XTyKxGgkfNLkgetHNnY_MU8K4iJMrugJZeUzdszP74L7UPYvAszD8T7kzxyyr5cIPuY36_Z3cC6iJdkthVCZ0QlAORe40PYrL", 0, v493);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v493, __LINE__);
	return failed;
}

int variant271() {
	int failed = 0;
	create_random_file("random168");
	FILE* v494 = fopen("random168", "wx");
	failed = 0;
	fputs("[272/432]fgets(NULL, Int(0), FILE* (File with random text, \"wx\"))\n", stdout);
	errno = 0;
	char* v1643 = fgets(NULL, 0, v494);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v494, __LINE__);
	return failed;
}

int variant272() {
	int failed = 0;
	create_random_file("random168");
	FILE* v494 = fopen("random168", "wx");
	failed = 0;
	fputs("[273/432]fgets(Random text, Int(0), FILE* (File with random text, \"wx\"))\n", stdout);
	errno = 0;
	char* v1644 = fgets("PiJN4erTdjLsdynjiYRCQcxrYCnyI0lSQCfw6JhO_0vUh7NOAxpgEmshzSFoe9cc5mdbz03hzkasp1wLQJ36_jr7HiqL2Jjsfwfv", 0, v494);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v494, __LINE__);
	return failed;
}

int variant273() {
	int failed = 0;
	remove_file("file169");
	FILE* v495 = fopen("file169", "ax");
	failed = 0;
	fputs("[274/432]fgets(NULL, Int(0), FILE* (File name, \"ax\"))\n", stdout);
	errno = 0;
	char* v1648 = fgets(NULL, 0, v495);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x03be6260, 0x00000000, 0, 0, 0x00000000, "", v495, __LINE__);
	return failed;
}

int variant274() {
	int failed = 0;
	remove_file("file169");
	FILE* v495 = fopen("file169", "ax");
	failed = 0;
	fputs("[275/432]fgets(Random text, Int(0), FILE* (File name, \"ax\"))\n", stdout);
	errno = 0;
	char* v1649 = fgets("OxDmiHx_ayo2Jwdo7AyDZ4d7ap7d0ZKGuvXLvuZPIyaUuSXEj9u9S2IfGFlXWCVvdzjwCq5d2GC9q8NLjLzwqUKyNPiBSMlcVr88", 0, v495);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa0628260, 0x00000000, 0, 0, 0x00000000, "", v495, __LINE__);
	return failed;
}

int variant275() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file169");
	FILE* v495 = fopen("file169", "ax");
	failed = 0;
	fputs("[276/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"ax\"))\n", stdout);
	errno = 0;
	char* v1650 = fgets(v432, 64, v495);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x438c92b0, 0x00000000, 0, 1, 0x00000000, "", v495, __LINE__);
	return failed;
}

int variant276() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file169");
	FILE* v495 = fopen("file169", "ax");
	failed = 0;
	fputs("[277/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"ax\"))\n", stdout);
	errno = 0;
	char* v1651 = fgets(v433, 64, v495);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x902302b0, 0x00000000, 0, 1, 0x00000000, "", v495, __LINE__);
	return failed;
}

int variant277() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file169");
	FILE* v495 = fopen("file169", "ax");
	failed = 0;
	fputs("[278/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"ax\"))\n", stdout);
	errno = 0;
	char* v1652 = fgets(v434, 64, v495);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xce3672b0, 0x00000000, 0, 1, 0x00000000, "", v495, __LINE__);
	return failed;
}

int variant278() {
	int failed = 0;
	create_empty_file("empty169");
	FILE* v496 = fopen("empty169", "ax");
	failed = 0;
	fputs("[279/432]fgets(NULL, Int(0), FILE* (Empty file, \"ax\"))\n", stdout);
	errno = 0;
	char* v1653 = fgets(NULL, 0, v496);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v496, __LINE__);
	return failed;
}

int variant279() {
	int failed = 0;
	create_empty_file("empty169");
	FILE* v496 = fopen("empty169", "ax");
	failed = 0;
	fputs("[280/432]fgets(Random text, Int(0), FILE* (Empty file, \"ax\"))\n", stdout);
	errno = 0;
	char* v1654 = fgets("mnaeYX7QpDueoNkIfv6uc5iwUmuDBZCZqoOfq4ek8ca0y1F3jLbCb20GAczeQLkCLXDtgoMyyW1GcozldbqlKL1Z00_QhVVtAYdB", 0, v496);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v496, __LINE__);
	return failed;
}

int variant280() {
	int failed = 0;
	create_random_file("random169");
	FILE* v497 = fopen("random169", "ax");
	failed = 0;
	fputs("[281/432]fgets(NULL, Int(0), FILE* (File with random text, \"ax\"))\n", stdout);
	errno = 0;
	char* v1658 = fgets(NULL, 0, v497);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v497, __LINE__);
	return failed;
}

int variant281() {
	int failed = 0;
	create_random_file("random169");
	FILE* v497 = fopen("random169", "ax");
	failed = 0;
	fputs("[282/432]fgets(Random text, Int(0), FILE* (File with random text, \"ax\"))\n", stdout);
	errno = 0;
	char* v1659 = fgets("6ExPKt1zRZtZlzDFUq3kZ9EQFeQyyG8qyKGsAhWlQRi0VLxiMZnCEKQ20z1oB74jU3p8q0BfOqyyKCspU6hOuytNKyXXpvkJpE5B", 0, v497);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v497, __LINE__);
	return failed;
}

int variant282() {
	int failed = 0;
	remove_file("file170");
	FILE* v498 = fopen("file170", "r+x");
	failed = 0;
	fputs("[283/432]fgets(NULL, Int(0), FILE* (File name, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1663 = fgets(NULL, 0, v498);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v498, __LINE__);
	return failed;
}

int variant283() {
	int failed = 0;
	remove_file("file170");
	FILE* v498 = fopen("file170", "r+x");
	failed = 0;
	fputs("[284/432]fgets(Random text, Int(0), FILE* (File name, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1664 = fgets("rdiXBsqCleuB3FbWXvB6r6i3XyQ0vPopzrq8sPwDDArzuro8ySAFGZLgNyhdPgbNrOJauMuIoxQldrfK_gKRnEeIwr5GzxTk_vrF", 0, v498);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v498, __LINE__);
	return failed;
}

int variant284() {
	int failed = 0;
	create_empty_file("empty170");
	FILE* v499 = fopen("empty170", "r+x");
	failed = 0;
	fputs("[285/432]fgets(NULL, Int(0), FILE* (Empty file, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1668 = fgets(NULL, 0, v499);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6b212260, 0x00000000, 0, 0, 0x00000000, "", v499, __LINE__);
	return failed;
}

int variant285() {
	int failed = 0;
	create_empty_file("empty170");
	FILE* v499 = fopen("empty170", "r+x");
	failed = 0;
	fputs("[286/432]fgets(Random text, Int(0), FILE* (Empty file, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1669 = fgets("Dea8PmP5_9SHPFjxRjV6CMmgl_DlpsT8zYlLvSrTzCtqe6ZBnW34WARSBH82Vi9IN0wmF7hxWBfIU109jXVD8KR_39sCusGdy0gC", 0, v499);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6eda5260, 0x00000000, 0, 0, 0x00000000, "", v499, __LINE__);
	return failed;
}

int variant286() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty170");
	FILE* v499 = fopen("empty170", "r+x");
	failed = 0;
	fputs("[287/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1670 = fgets(v432, 64, v499);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1f2812b0, 0x00000000, 1, 0, 0x00000000, "", v499, __LINE__);
	return failed;
}

int variant287() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty170");
	FILE* v499 = fopen("empty170", "r+x");
	failed = 0;
	fputs("[288/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1671 = fgets(v433, 64, v499);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb70c32b0, 0x00000000, 1, 0, 0x00000000, "", v499, __LINE__);
	return failed;
}

int variant288() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty170");
	FILE* v499 = fopen("empty170", "r+x");
	failed = 0;
	fputs("[289/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1672 = fgets(v434, 64, v499);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe4d592b0, 0x00000000, 1, 0, 0x00000000, "", v499, __LINE__);
	return failed;
}

int variant289() {
	int failed = 0;
	create_random_file("random170");
	FILE* v500 = fopen("random170", "r+x");
	failed = 0;
	fputs("[290/432]fgets(NULL, Int(0), FILE* (File with random text, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1673 = fgets(NULL, 0, v500);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3aef8260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v500, __LINE__);
	return failed;
}

int variant290() {
	int failed = 0;
	create_random_file("random170");
	FILE* v500 = fopen("random170", "r+x");
	failed = 0;
	fputs("[291/432]fgets(Random text, Int(0), FILE* (File with random text, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1674 = fgets("lAvjoy73CNeKdafUTzJxnLdXtBxGiXFydRg51zttTEueVt2E7XPlioLYA3PmdA3wgy7kakYXOAVGKro6qeBzBsy_QJs87kF6Rjv_", 0, v500);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbaf09260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v500, __LINE__);
	return failed;
}

int variant291() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random170");
	FILE* v500 = fopen("random170", "r+x");
	failed = 0;
	fputs("[292/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1675 = fgets(v432, 64, v500);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xde26c2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v500, __LINE__);
	return failed;
}

int variant292() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random170");
	FILE* v500 = fopen("random170", "r+x");
	failed = 0;
	fputs("[293/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1676 = fgets(v433, 64, v500);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x250062b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v500, __LINE__);
	return failed;
}

int variant293() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random170");
	FILE* v500 = fopen("random170", "r+x");
	failed = 0;
	fputs("[294/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r+x\"))\n", stdout);
	errno = 0;
	char* v1677 = fgets(v434, 64, v500);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x46fb22b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v500, __LINE__);
	return failed;
}

int variant294() {
	int failed = 0;
	remove_file("file171");
	FILE* v501 = fopen("file171", "w+x");
	failed = 0;
	fputs("[295/432]fgets(NULL, Int(0), FILE* (File name, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1678 = fgets(NULL, 0, v501);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2aa96260, 0x00000000, 0, 0, 0x00000000, "", v501, __LINE__);
	return failed;
}

int variant295() {
	int failed = 0;
	remove_file("file171");
	FILE* v501 = fopen("file171", "w+x");
	failed = 0;
	fputs("[296/432]fgets(Random text, Int(0), FILE* (File name, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1679 = fgets("yco7joxu0FZ6V_Xa1hQEsOGBQvsrVt_JpEDFbUm4Kp393OFVtn81qF9ousCJwsPqwsKuKmjckQJYyV_cYxOP9IQRu1cDj2jmRF12", 0, v501);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x25c67260, 0x00000000, 0, 0, 0x00000000, "", v501, __LINE__);
	return failed;
}

int variant296() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file171");
	FILE* v501 = fopen("file171", "w+x");
	failed = 0;
	fputs("[297/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1680 = fgets(v432, 64, v501);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x594832b0, 0x00000000, 1, 0, 0x00000000, "", v501, __LINE__);
	return failed;
}

int variant297() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file171");
	FILE* v501 = fopen("file171", "w+x");
	failed = 0;
	fputs("[298/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1681 = fgets(v433, 64, v501);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x15a172b0, 0x00000000, 1, 0, 0x00000000, "", v501, __LINE__);
	return failed;
}

int variant298() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file171");
	FILE* v501 = fopen("file171", "w+x");
	failed = 0;
	fputs("[299/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1682 = fgets(v434, 64, v501);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe530a2b0, 0x00000000, 1, 0, 0x00000000, "", v501, __LINE__);
	return failed;
}

int variant299() {
	int failed = 0;
	create_empty_file("empty171");
	FILE* v502 = fopen("empty171", "w+x");
	failed = 0;
	fputs("[300/432]fgets(NULL, Int(0), FILE* (Empty file, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1683 = fgets(NULL, 0, v502);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v502, __LINE__);
	return failed;
}

int variant300() {
	int failed = 0;
	create_empty_file("empty171");
	FILE* v502 = fopen("empty171", "w+x");
	failed = 0;
	fputs("[301/432]fgets(Random text, Int(0), FILE* (Empty file, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1684 = fgets("Emt2tKkPbGJf10BQhaQ2qy8OgI1Skfn8Fc3KbYjWCzpjqiSaQAbUGofyOtHg0liaq9fzAy1d1q7WqYGpq4Pn0b5BM0BmKKzmB6lj", 0, v502);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v502, __LINE__);
	return failed;
}

int variant301() {
	int failed = 0;
	create_random_file("random171");
	FILE* v503 = fopen("random171", "w+x");
	failed = 0;
	fputs("[302/432]fgets(NULL, Int(0), FILE* (File with random text, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1688 = fgets(NULL, 0, v503);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v503, __LINE__);
	return failed;
}

int variant302() {
	int failed = 0;
	create_random_file("random171");
	FILE* v503 = fopen("random171", "w+x");
	failed = 0;
	fputs("[303/432]fgets(Random text, Int(0), FILE* (File with random text, \"w+x\"))\n", stdout);
	errno = 0;
	char* v1689 = fgets("VKL0Ru_664GzwAl8NJken0qAt8yEV0l3Y7jK5NLfRkTqTcU4xwkVj8qigHh_YV6WcJf3rkKmHosvOjzsPUnp_oNniCz78jmK92AO", 0, v503);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v503, __LINE__);
	return failed;
}

int variant303() {
	int failed = 0;
	remove_file("file172");
	FILE* v504 = fopen("file172", "a+x");
	failed = 0;
	fputs("[304/432]fgets(NULL, Int(0), FILE* (File name, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1693 = fgets(NULL, 0, v504);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb9c35260, 0x00000000, 0, 0, 0x00000000, "", v504, __LINE__);
	return failed;
}

int variant304() {
	int failed = 0;
	remove_file("file172");
	FILE* v504 = fopen("file172", "a+x");
	failed = 0;
	fputs("[305/432]fgets(Random text, Int(0), FILE* (File name, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1694 = fgets("2NjNf5kon3GC7vfMoa4NsFeDpWMCsOyD8H1poxTHuUtjV0yRg_hqlJi3Uj978myr43V7KpH5kWUcruSzgldZnZwM0X2fOcEMUj4G", 0, v504);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3d215260, 0x00000000, 0, 0, 0x00000000, "", v504, __LINE__);
	return failed;
}

int variant305() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file172");
	FILE* v504 = fopen("file172", "a+x");
	failed = 0;
	fputs("[306/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1695 = fgets(v432, 64, v504);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x452fc2b0, 0x00000000, 1, 0, 0x00000000, "", v504, __LINE__);
	return failed;
}

int variant306() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file172");
	FILE* v504 = fopen("file172", "a+x");
	failed = 0;
	fputs("[307/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1696 = fgets(v433, 64, v504);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xcd8aa2b0, 0x00000000, 1, 0, 0x00000000, "", v504, __LINE__);
	return failed;
}

int variant307() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file172");
	FILE* v504 = fopen("file172", "a+x");
	failed = 0;
	fputs("[308/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1697 = fgets(v434, 64, v504);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd0a892b0, 0x00000000, 1, 0, 0x00000000, "", v504, __LINE__);
	return failed;
}

int variant308() {
	int failed = 0;
	create_empty_file("empty172");
	FILE* v505 = fopen("empty172", "a+x");
	failed = 0;
	fputs("[309/432]fgets(NULL, Int(0), FILE* (Empty file, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1698 = fgets(NULL, 0, v505);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v505, __LINE__);
	return failed;
}

int variant309() {
	int failed = 0;
	create_empty_file("empty172");
	FILE* v505 = fopen("empty172", "a+x");
	failed = 0;
	fputs("[310/432]fgets(Random text, Int(0), FILE* (Empty file, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1699 = fgets("63zSMQo6cO0e3kWa8jZmAn7p1zBu2PIgupW93hXPXumadJTq_sHN3GwHZ7PpEfm7FxT2GmVuMyVflXgFJNz7oZ4QGypQJokvT5Wy", 0, v505);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v505, __LINE__);
	return failed;
}

int variant310() {
	int failed = 0;
	create_random_file("random172");
	FILE* v506 = fopen("random172", "a+x");
	failed = 0;
	fputs("[311/432]fgets(NULL, Int(0), FILE* (File with random text, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1703 = fgets(NULL, 0, v506);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v506, __LINE__);
	return failed;
}

int variant311() {
	int failed = 0;
	create_random_file("random172");
	FILE* v506 = fopen("random172", "a+x");
	failed = 0;
	fputs("[312/432]fgets(Random text, Int(0), FILE* (File with random text, \"a+x\"))\n", stdout);
	errno = 0;
	char* v1704 = fgets("Kfcqd_KEQwAkjONBw54jBQRw1Z0CMElS4oT9Dx3oC3AwB9n5BcGhfi4n4AEfrYTFaI1JGjDFYF4lRIcHYwhbQz8ZPvqK80KPNe93", 0, v506);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v506, __LINE__);
	return failed;
}

int variant312() {
	int failed = 0;
	remove_file("file173");
	FILE* v507 = fopen("file173", "rtx");
	failed = 0;
	fputs("[313/432]fgets(NULL, Int(0), FILE* (File name, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1708 = fgets(NULL, 0, v507);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v507, __LINE__);
	return failed;
}

int variant313() {
	int failed = 0;
	remove_file("file173");
	FILE* v507 = fopen("file173", "rtx");
	failed = 0;
	fputs("[314/432]fgets(Random text, Int(0), FILE* (File name, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1709 = fgets("84KltizGZSgOnngaMHoLe64bxwS5LLTzuN3_YdMXvNq4DzcdGh62UhLoqRs6b2exYWVAj8tzpjK_SOLFr1l7zks9SDSu1QyohYbY", 0, v507);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v507, __LINE__);
	return failed;
}

int variant314() {
	int failed = 0;
	create_empty_file("empty173");
	FILE* v508 = fopen("empty173", "rtx");
	failed = 0;
	fputs("[315/432]fgets(NULL, Int(0), FILE* (Empty file, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1713 = fgets(NULL, 0, v508);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb654d260, 0x00000000, 0, 0, 0x00000000, "", v508, __LINE__);
	return failed;
}

int variant315() {
	int failed = 0;
	create_empty_file("empty173");
	FILE* v508 = fopen("empty173", "rtx");
	failed = 0;
	fputs("[316/432]fgets(Random text, Int(0), FILE* (Empty file, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1714 = fgets("kifjVbu6v_v1ue7ecvNB5ec0zzB_i_SWYBbr9GqGxyqhCq7tKVGW6daUhXUWtZmdkIsNok_POuz_R5V49kBm0IL6AqWip1RoOZ1B", 0, v508);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xeb5f8260, 0x00000000, 0, 0, 0x00000000, "", v508, __LINE__);
	return failed;
}

int variant316() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty173");
	FILE* v508 = fopen("empty173", "rtx");
	failed = 0;
	fputs("[317/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1715 = fgets(v432, 64, v508);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xc55672b0, 0x00000000, 1, 0, 0x00000000, "", v508, __LINE__);
	return failed;
}

int variant317() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty173");
	FILE* v508 = fopen("empty173", "rtx");
	failed = 0;
	fputs("[318/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1716 = fgets(v433, 64, v508);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbe8252b0, 0x00000000, 1, 0, 0x00000000, "", v508, __LINE__);
	return failed;
}

int variant318() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty173");
	FILE* v508 = fopen("empty173", "rtx");
	failed = 0;
	fputs("[319/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1717 = fgets(v434, 64, v508);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6aa962b0, 0x00000000, 1, 0, 0x00000000, "", v508, __LINE__);
	return failed;
}

int variant319() {
	int failed = 0;
	create_random_file("random173");
	FILE* v509 = fopen("random173", "rtx");
	failed = 0;
	fputs("[320/432]fgets(NULL, Int(0), FILE* (File with random text, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1718 = fgets(NULL, 0, v509);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdeebc260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v509, __LINE__);
	return failed;
}

int variant320() {
	int failed = 0;
	create_random_file("random173");
	FILE* v509 = fopen("random173", "rtx");
	failed = 0;
	fputs("[321/432]fgets(Random text, Int(0), FILE* (File with random text, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1719 = fgets("HdysXp4KXsRSXvY8LrvVUjm7clyhZrZEnbMHSSyir4zpPqK_XdKTL1SH2xMe255VNY4xqh4dD43IJEhv7rs5_ggyRVCr8hrja1ZJ", 0, v509);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x07e1a260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v509, __LINE__);
	return failed;
}

int variant321() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random173");
	FILE* v509 = fopen("random173", "rtx");
	failed = 0;
	fputs("[322/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1720 = fgets(v432, 64, v509);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2b29b2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v509, __LINE__);
	return failed;
}

int variant322() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random173");
	FILE* v509 = fopen("random173", "rtx");
	failed = 0;
	fputs("[323/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1721 = fgets(v433, 64, v509);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xcbecc2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v509, __LINE__);
	return failed;
}

int variant323() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random173");
	FILE* v509 = fopen("random173", "rtx");
	failed = 0;
	fputs("[324/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"rtx\"))\n", stdout);
	errno = 0;
	char* v1722 = fgets(v434, 64, v509);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x57fdb2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v509, __LINE__);
	return failed;
}

int variant324() {
	int failed = 0;
	remove_file("file174");
	FILE* v510 = fopen("file174", "wtx");
	failed = 0;
	fputs("[325/432]fgets(NULL, Int(0), FILE* (File name, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1723 = fgets(NULL, 0, v510);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb957e260, 0x00000000, 0, 0, 0x00000000, "", v510, __LINE__);
	return failed;
}

int variant325() {
	int failed = 0;
	remove_file("file174");
	FILE* v510 = fopen("file174", "wtx");
	failed = 0;
	fputs("[326/432]fgets(Random text, Int(0), FILE* (File name, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1724 = fgets("ulgHrU_Fkxn_JFaNc_KRzySQpcGNOF2JGRR96ZVteRanKdVxHGcXA4N39vDVSncsdjype7VU00cGAjaLwLnvwWkWTPAZaQ5JSOch", 0, v510);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xa08f6260, 0x00000000, 0, 0, 0x00000000, "", v510, __LINE__);
	return failed;
}

int variant326() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file174");
	FILE* v510 = fopen("file174", "wtx");
	failed = 0;
	fputs("[327/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1725 = fgets(v432, 64, v510);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6ee342b0, 0x00000000, 0, 1, 0x00000000, "", v510, __LINE__);
	return failed;
}

int variant327() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file174");
	FILE* v510 = fopen("file174", "wtx");
	failed = 0;
	fputs("[328/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1726 = fgets(v433, 64, v510);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x671f12b0, 0x00000000, 0, 1, 0x00000000, "", v510, __LINE__);
	return failed;
}

int variant328() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file174");
	FILE* v510 = fopen("file174", "wtx");
	failed = 0;
	fputs("[329/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1727 = fgets(v434, 64, v510);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x12b8c2b0, 0x00000000, 0, 1, 0x00000000, "", v510, __LINE__);
	return failed;
}

int variant329() {
	int failed = 0;
	create_empty_file("empty174");
	FILE* v511 = fopen("empty174", "wtx");
	failed = 0;
	fputs("[330/432]fgets(NULL, Int(0), FILE* (Empty file, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1728 = fgets(NULL, 0, v511);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v511, __LINE__);
	return failed;
}

int variant330() {
	int failed = 0;
	create_empty_file("empty174");
	FILE* v511 = fopen("empty174", "wtx");
	failed = 0;
	fputs("[331/432]fgets(Random text, Int(0), FILE* (Empty file, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1729 = fgets("mq2mJzMNB6CIZQD_FSiyA8tFD7oZ7oJLKoMet2ZZumz6wfTC0FNg3lJ65DjV0dYFIU15RKO12osenQ55OziLv7wjeMKFpmlCcde8", 0, v511);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v511, __LINE__);
	return failed;
}

int variant331() {
	int failed = 0;
	create_random_file("random174");
	FILE* v512 = fopen("random174", "wtx");
	failed = 0;
	fputs("[332/432]fgets(NULL, Int(0), FILE* (File with random text, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1733 = fgets(NULL, 0, v512);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v512, __LINE__);
	return failed;
}

int variant332() {
	int failed = 0;
	create_random_file("random174");
	FILE* v512 = fopen("random174", "wtx");
	failed = 0;
	fputs("[333/432]fgets(Random text, Int(0), FILE* (File with random text, \"wtx\"))\n", stdout);
	errno = 0;
	char* v1734 = fgets("qvzFUZqKyLWJoM45yPebhoScxlQ9ZpUjr2uGl7xa5y5ARjgM4M44HqWiT4AoD1a6q61HsjufAEf8heuv8S3Aglz8kl7bPTje6x18", 0, v512);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v512, __LINE__);
	return failed;
}

int variant333() {
	int failed = 0;
	remove_file("file175");
	FILE* v513 = fopen("file175", "atx");
	failed = 0;
	fputs("[334/432]fgets(NULL, Int(0), FILE* (File name, \"atx\"))\n", stdout);
	errno = 0;
	char* v1738 = fgets(NULL, 0, v513);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x11c10260, 0x00000000, 0, 0, 0x00000000, "", v513, __LINE__);
	return failed;
}

int variant334() {
	int failed = 0;
	remove_file("file175");
	FILE* v513 = fopen("file175", "atx");
	failed = 0;
	fputs("[335/432]fgets(Random text, Int(0), FILE* (File name, \"atx\"))\n", stdout);
	errno = 0;
	char* v1739 = fgets("gl3L9LeWqc4u4JjDjsABizgCrC4kbe5CzzUVGJ6sHILpE88ld_6M11MBfAdZpyUZo4obRZy0GjOf8CbEG55HnIW8olZc7PL3URRp", 0, v513);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdd35c260, 0x00000000, 0, 0, 0x00000000, "", v513, __LINE__);
	return failed;
}

int variant335() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file175");
	FILE* v513 = fopen("file175", "atx");
	failed = 0;
	fputs("[336/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"atx\"))\n", stdout);
	errno = 0;
	char* v1740 = fgets(v432, 64, v513);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x26d0b2b0, 0x00000000, 0, 1, 0x00000000, "", v513, __LINE__);
	return failed;
}

int variant336() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file175");
	FILE* v513 = fopen("file175", "atx");
	failed = 0;
	fputs("[337/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"atx\"))\n", stdout);
	errno = 0;
	char* v1741 = fgets(v433, 64, v513);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x553ca2b0, 0x00000000, 0, 1, 0x00000000, "", v513, __LINE__);
	return failed;
}

int variant337() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file175");
	FILE* v513 = fopen("file175", "atx");
	failed = 0;
	fputs("[338/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"atx\"))\n", stdout);
	errno = 0;
	char* v1742 = fgets(v434, 64, v513);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xaa26b2b0, 0x00000000, 0, 1, 0x00000000, "", v513, __LINE__);
	return failed;
}

int variant338() {
	int failed = 0;
	create_empty_file("empty175");
	FILE* v514 = fopen("empty175", "atx");
	failed = 0;
	fputs("[339/432]fgets(NULL, Int(0), FILE* (Empty file, \"atx\"))\n", stdout);
	errno = 0;
	char* v1743 = fgets(NULL, 0, v514);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v514, __LINE__);
	return failed;
}

int variant339() {
	int failed = 0;
	create_empty_file("empty175");
	FILE* v514 = fopen("empty175", "atx");
	failed = 0;
	fputs("[340/432]fgets(Random text, Int(0), FILE* (Empty file, \"atx\"))\n", stdout);
	errno = 0;
	char* v1744 = fgets("clHd3f2SBDq0QtJt76d21JUTcHqR51F__HBVzcvDmcUZvnawJWQnzUA2JXzmZm2sonYtMmHWNNoCRFILqxSw3isOhBz_UIapZnyh", 0, v514);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v514, __LINE__);
	return failed;
}

int variant340() {
	int failed = 0;
	create_random_file("random175");
	FILE* v515 = fopen("random175", "atx");
	failed = 0;
	fputs("[341/432]fgets(NULL, Int(0), FILE* (File with random text, \"atx\"))\n", stdout);
	errno = 0;
	char* v1748 = fgets(NULL, 0, v515);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v515, __LINE__);
	return failed;
}

int variant341() {
	int failed = 0;
	create_random_file("random175");
	FILE* v515 = fopen("random175", "atx");
	failed = 0;
	fputs("[342/432]fgets(Random text, Int(0), FILE* (File with random text, \"atx\"))\n", stdout);
	errno = 0;
	char* v1749 = fgets("Nl_rPU5RyP3lazKauHe9eV_c7tqlvCQTaTAVOf7SduJbWKMZxwtCiyKr2_ukmICjhbxF4LK_jIyUjUaBltbfqFuvb6rwn5PzK8Gz", 0, v515);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v515, __LINE__);
	return failed;
}

int variant342() {
	int failed = 0;
	remove_file("file176");
	FILE* v516 = fopen("file176", "r+tx");
	failed = 0;
	fputs("[343/432]fgets(NULL, Int(0), FILE* (File name, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1753 = fgets(NULL, 0, v516);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v516, __LINE__);
	return failed;
}

int variant343() {
	int failed = 0;
	remove_file("file176");
	FILE* v516 = fopen("file176", "r+tx");
	failed = 0;
	fputs("[344/432]fgets(Random text, Int(0), FILE* (File name, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1754 = fgets("Ty84UV5UryLFgRG7l5_ig_2evwmPmQSPJzmSI_asP0MPFV2U7j9r4_eQOLuChvLdBtf91ShkY408PPkiHOKsNWCUISrEelEXNova", 0, v516);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v516, __LINE__);
	return failed;
}

int variant344() {
	int failed = 0;
	create_empty_file("empty176");
	FILE* v517 = fopen("empty176", "r+tx");
	failed = 0;
	fputs("[345/432]fgets(NULL, Int(0), FILE* (Empty file, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1758 = fgets(NULL, 0, v517);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x05cdf260, 0x00000000, 0, 0, 0x00000000, "", v517, __LINE__);
	return failed;
}

int variant345() {
	int failed = 0;
	create_empty_file("empty176");
	FILE* v517 = fopen("empty176", "r+tx");
	failed = 0;
	fputs("[346/432]fgets(Random text, Int(0), FILE* (Empty file, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1759 = fgets("FTqq_zf2BeCiRbK0j_BY0yFgoqWZAsocUrEB2PsQUhaK3orVhfjijrEPvM5Wlikic8Gt11sWD3mZEXlEgoqdyGjSBFI4Bl5Y_Xta", 0, v517);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd2878260, 0x00000000, 0, 0, 0x00000000, "", v517, __LINE__);
	return failed;
}

int variant346() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty176");
	FILE* v517 = fopen("empty176", "r+tx");
	failed = 0;
	fputs("[347/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1760 = fgets(v432, 64, v517);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x644932b0, 0x00000000, 1, 0, 0x00000000, "", v517, __LINE__);
	return failed;
}

int variant347() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty176");
	FILE* v517 = fopen("empty176", "r+tx");
	failed = 0;
	fputs("[348/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1761 = fgets(v433, 64, v517);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x736712b0, 0x00000000, 1, 0, 0x00000000, "", v517, __LINE__);
	return failed;
}

int variant348() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty176");
	FILE* v517 = fopen("empty176", "r+tx");
	failed = 0;
	fputs("[349/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1762 = fgets(v434, 64, v517);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe92762b0, 0x00000000, 1, 0, 0x00000000, "", v517, __LINE__);
	return failed;
}

int variant349() {
	int failed = 0;
	create_random_file("random176");
	FILE* v518 = fopen("random176", "r+tx");
	failed = 0;
	fputs("[350/432]fgets(NULL, Int(0), FILE* (File with random text, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1763 = fgets(NULL, 0, v518);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x1d103260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v518, __LINE__);
	return failed;
}

int variant350() {
	int failed = 0;
	create_random_file("random176");
	FILE* v518 = fopen("random176", "r+tx");
	failed = 0;
	fputs("[351/432]fgets(Random text, Int(0), FILE* (File with random text, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1764 = fgets("ImRlgvHuYemkYG__rWj48Qt7LTYygpbGIiuhDaG0j2suisOuQ1ydEb_EVHm4ygwQGWV5T1dRzsTaM8gDxl04fHZmDLx8ej4wIlaB", 0, v518);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x00344260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v518, __LINE__);
	return failed;
}

int variant351() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random176");
	FILE* v518 = fopen("random176", "r+tx");
	failed = 0;
	fputs("[352/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1765 = fgets(v432, 64, v518);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x103912b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v518, __LINE__);
	return failed;
}

int variant352() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random176");
	FILE* v518 = fopen("random176", "r+tx");
	failed = 0;
	fputs("[353/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1766 = fgets(v433, 64, v518);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6d41c2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v518, __LINE__);
	return failed;
}

int variant353() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random176");
	FILE* v518 = fopen("random176", "r+tx");
	failed = 0;
	fputs("[354/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r+tx\"))\n", stdout);
	errno = 0;
	char* v1767 = fgets(v434, 64, v518);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe2fbb2b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v518, __LINE__);
	return failed;
}

int variant354() {
	int failed = 0;
	remove_file("file177");
	FILE* v519 = fopen("file177", "w+tx");
	failed = 0;
	fputs("[355/432]fgets(NULL, Int(0), FILE* (File name, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1768 = fgets(NULL, 0, v519);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5f448260, 0x00000000, 0, 0, 0x00000000, "", v519, __LINE__);
	return failed;
}

int variant355() {
	int failed = 0;
	remove_file("file177");
	FILE* v519 = fopen("file177", "w+tx");
	failed = 0;
	fputs("[356/432]fgets(Random text, Int(0), FILE* (File name, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1769 = fgets("egHnG623dbz8CQZRto6y0Wcvco6hp3hGAvC0uLJe1hqCQJp2h36srGVBoYYy9nGzP4vfUSoEGpfPJsQ9b1nYI_iwCF902o4OHhXS", 0, v519);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5b179260, 0x00000000, 0, 0, 0x00000000, "", v519, __LINE__);
	return failed;
}

int variant356() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file177");
	FILE* v519 = fopen("file177", "w+tx");
	failed = 0;
	fputs("[357/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1770 = fgets(v432, 64, v519);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x939a02b0, 0x00000000, 1, 0, 0x00000000, "", v519, __LINE__);
	return failed;
}

int variant357() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file177");
	FILE* v519 = fopen("file177", "w+tx");
	failed = 0;
	fputs("[358/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1771 = fgets(v433, 64, v519);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6a97f2b0, 0x00000000, 1, 0, 0x00000000, "", v519, __LINE__);
	return failed;
}

int variant358() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file177");
	FILE* v519 = fopen("file177", "w+tx");
	failed = 0;
	fputs("[359/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1772 = fgets(v434, 64, v519);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xce8b52b0, 0x00000000, 1, 0, 0x00000000, "", v519, __LINE__);
	return failed;
}

int variant359() {
	int failed = 0;
	create_empty_file("empty177");
	FILE* v520 = fopen("empty177", "w+tx");
	failed = 0;
	fputs("[360/432]fgets(NULL, Int(0), FILE* (Empty file, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1773 = fgets(NULL, 0, v520);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v520, __LINE__);
	return failed;
}

int variant360() {
	int failed = 0;
	create_empty_file("empty177");
	FILE* v520 = fopen("empty177", "w+tx");
	failed = 0;
	fputs("[361/432]fgets(Random text, Int(0), FILE* (Empty file, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1774 = fgets("7JK84AdDY40yDOwu2EXaGS6L2l3zRbbO__4pX7c2fo15wwz2x8UnK1z0CXf67I5H1oF0l9qpTScp9eIlCX8I8sQiA4DE3nJQw8hN", 0, v520);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v520, __LINE__);
	return failed;
}

int variant361() {
	int failed = 0;
	create_random_file("random177");
	FILE* v521 = fopen("random177", "w+tx");
	failed = 0;
	fputs("[362/432]fgets(NULL, Int(0), FILE* (File with random text, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1778 = fgets(NULL, 0, v521);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v521, __LINE__);
	return failed;
}

int variant362() {
	int failed = 0;
	create_random_file("random177");
	FILE* v521 = fopen("random177", "w+tx");
	failed = 0;
	fputs("[363/432]fgets(Random text, Int(0), FILE* (File with random text, \"w+tx\"))\n", stdout);
	errno = 0;
	char* v1779 = fgets("uNjf5w65IuwtXNt8pQOoBJxHacUURegKCfN3Qv7tppmjaZzZlYxzQ19QuIPsFgMbbdOCxB5JdKNhyUc54DhX5gYOSiXRZ8ioteRy", 0, v521);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v521, __LINE__);
	return failed;
}

int variant363() {
	int failed = 0;
	remove_file("file178");
	FILE* v522 = fopen("file178", "a+tx");
	failed = 0;
	fputs("[364/432]fgets(NULL, Int(0), FILE* (File name, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1783 = fgets(NULL, 0, v522);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x85d50260, 0x00000000, 0, 0, 0x00000000, "", v522, __LINE__);
	return failed;
}

int variant364() {
	int failed = 0;
	remove_file("file178");
	FILE* v522 = fopen("file178", "a+tx");
	failed = 0;
	fputs("[365/432]fgets(Random text, Int(0), FILE* (File name, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1784 = fgets("lbP5Mb_VhFuGku0vnLiOTGqWoV8rz5WqgSpViGLHeD7tcWa6YAYxlowYgO3kqU4ZdUav7I7dZZu08ugwgz8yK3nNBO8sRn22olh1", 0, v522);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x7647f260, 0x00000000, 0, 0, 0x00000000, "", v522, __LINE__);
	return failed;
}

int variant365() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file178");
	FILE* v522 = fopen("file178", "a+tx");
	failed = 0;
	fputs("[366/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1785 = fgets(v432, 64, v522);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe377a2b0, 0x00000000, 1, 0, 0x00000000, "", v522, __LINE__);
	return failed;
}

int variant366() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file178");
	FILE* v522 = fopen("file178", "a+tx");
	failed = 0;
	fputs("[367/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1786 = fgets(v433, 64, v522);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6bf192b0, 0x00000000, 1, 0, 0x00000000, "", v522, __LINE__);
	return failed;
}

int variant367() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file178");
	FILE* v522 = fopen("file178", "a+tx");
	failed = 0;
	fputs("[368/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1787 = fgets(v434, 64, v522);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8da8a2b0, 0x00000000, 1, 0, 0x00000000, "", v522, __LINE__);
	return failed;
}

int variant368() {
	int failed = 0;
	create_empty_file("empty178");
	FILE* v523 = fopen("empty178", "a+tx");
	failed = 0;
	fputs("[369/432]fgets(NULL, Int(0), FILE* (Empty file, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1788 = fgets(NULL, 0, v523);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v523, __LINE__);
	return failed;
}

int variant369() {
	int failed = 0;
	create_empty_file("empty178");
	FILE* v523 = fopen("empty178", "a+tx");
	failed = 0;
	fputs("[370/432]fgets(Random text, Int(0), FILE* (Empty file, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1789 = fgets("Bel2oWg3pmZ_oU_Kn4jNQx6NxAd98Qg_sqGz3LygV97u5TB32UeCOts9ApclPcRGZIfoHRYomNSsOifn1cgWwcOR2qgw04i2pQqU", 0, v523);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v523, __LINE__);
	return failed;
}

int variant370() {
	int failed = 0;
	create_random_file("random178");
	FILE* v524 = fopen("random178", "a+tx");
	failed = 0;
	fputs("[371/432]fgets(NULL, Int(0), FILE* (File with random text, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1793 = fgets(NULL, 0, v524);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v524, __LINE__);
	return failed;
}

int variant371() {
	int failed = 0;
	create_random_file("random178");
	FILE* v524 = fopen("random178", "a+tx");
	failed = 0;
	fputs("[372/432]fgets(Random text, Int(0), FILE* (File with random text, \"a+tx\"))\n", stdout);
	errno = 0;
	char* v1794 = fgets("gUJNTSxh0ElseUWTg5_DxeQtEbsS_YhywBI2Hk5RHVolhn6IGn_jDCqqmzAyODl57W2XCp7bffCg8AxD2gMe2F6LIXOX0uprJiL5", 0, v524);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v524, __LINE__);
	return failed;
}

int variant372() {
	int failed = 0;
	remove_file("file179");
	FILE* v525 = fopen("file179", "rbx");
	failed = 0;
	fputs("[373/432]fgets(NULL, Int(0), FILE* (File name, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1798 = fgets(NULL, 0, v525);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v525, __LINE__);
	return failed;
}

int variant373() {
	int failed = 0;
	remove_file("file179");
	FILE* v525 = fopen("file179", "rbx");
	failed = 0;
	fputs("[374/432]fgets(Random text, Int(0), FILE* (File name, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1799 = fgets("60OwyV55yUfWgjHLJzVFk_1DMweXgcWmiCzIgVYyGqBeXb4CaXX8wJW7Cn3kvh7t0aUAwJ2LSdtJ3PqTkpp2Q34GQYpxcYwgru5v", 0, v525);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v525, __LINE__);
	return failed;
}

int variant374() {
	int failed = 0;
	create_empty_file("empty179");
	FILE* v526 = fopen("empty179", "rbx");
	failed = 0;
	fputs("[375/432]fgets(NULL, Int(0), FILE* (Empty file, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1803 = fgets(NULL, 0, v526);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbf412260, 0x00000000, 0, 0, 0x00000000, "", v526, __LINE__);
	return failed;
}

int variant375() {
	int failed = 0;
	create_empty_file("empty179");
	FILE* v526 = fopen("empty179", "rbx");
	failed = 0;
	fputs("[376/432]fgets(Random text, Int(0), FILE* (Empty file, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1804 = fgets("6NIS8pBLBYTAO26CykiCUH5UYWD7pZ_RPQM5_QOgz8IGOv2bJ2n8OUqX1C0YY7jyOXnFtgkzxuuh8pumRfZaTmzLrdbTjfxtcBhW", 0, v526);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x845a9260, 0x00000000, 0, 0, 0x00000000, "", v526, __LINE__);
	return failed;
}

int variant376() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty179");
	FILE* v526 = fopen("empty179", "rbx");
	failed = 0;
	fputs("[377/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1805 = fgets(v432, 64, v526);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x767232b0, 0x00000000, 1, 0, 0x00000000, "", v526, __LINE__);
	return failed;
}

int variant377() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty179");
	FILE* v526 = fopen("empty179", "rbx");
	failed = 0;
	fputs("[378/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1806 = fgets(v433, 64, v526);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x53e822b0, 0x00000000, 1, 0, 0x00000000, "", v526, __LINE__);
	return failed;
}

int variant378() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty179");
	FILE* v526 = fopen("empty179", "rbx");
	failed = 0;
	fputs("[379/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1807 = fgets(v434, 64, v526);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x213ba2b0, 0x00000000, 1, 0, 0x00000000, "", v526, __LINE__);
	return failed;
}

int variant379() {
	int failed = 0;
	create_random_file("random179");
	FILE* v527 = fopen("random179", "rbx");
	failed = 0;
	fputs("[380/432]fgets(NULL, Int(0), FILE* (File with random text, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1808 = fgets(NULL, 0, v527);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x34497260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v527, __LINE__);
	return failed;
}

int variant380() {
	int failed = 0;
	create_random_file("random179");
	FILE* v527 = fopen("random179", "rbx");
	failed = 0;
	fputs("[381/432]fgets(Random text, Int(0), FILE* (File with random text, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1809 = fgets("FpozSYCGPjA294lKJjJkTDq76IkvhlZMAdquxRG9bL1WKyKbg0FCravtUzYV8pI8WfqHf4BV9vI0e6TJhun5r687d7FlHVanTG92", 0, v527);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x103ee260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v527, __LINE__);
	return failed;
}

int variant381() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random179");
	FILE* v527 = fopen("random179", "rbx");
	failed = 0;
	fputs("[382/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1810 = fgets(v432, 64, v527);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x46f752b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v527, __LINE__);
	return failed;
}

int variant382() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random179");
	FILE* v527 = fopen("random179", "rbx");
	failed = 0;
	fputs("[383/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1811 = fgets(v433, 64, v527);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x8fa722b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v527, __LINE__);
	return failed;
}

int variant383() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random179");
	FILE* v527 = fopen("random179", "rbx");
	failed = 0;
	fputs("[384/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"rbx\"))\n", stdout);
	errno = 0;
	char* v1812 = fgets(v434, 64, v527);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xaa5252b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v527, __LINE__);
	return failed;
}

int variant384() {
	int failed = 0;
	remove_file("file180");
	FILE* v528 = fopen("file180", "wbx");
	failed = 0;
	fputs("[385/432]fgets(NULL, Int(0), FILE* (File name, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1813 = fgets(NULL, 0, v528);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xeba1c260, 0x00000000, 0, 0, 0x00000000, "", v528, __LINE__);
	return failed;
}

int variant385() {
	int failed = 0;
	remove_file("file180");
	FILE* v528 = fopen("file180", "wbx");
	failed = 0;
	fputs("[386/432]fgets(Random text, Int(0), FILE* (File name, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1814 = fgets("dEU4xtPzJAzjrJi1l9ksEOXd5Kl_KEsE_QcsdSx0E_Zi9mGnyhBfTwOk12iVHm6jDaXb5tneWttw3Hgh9tj02zRwzsWmDAMFJdzb", 0, v528);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x671e5260, 0x00000000, 0, 0, 0x00000000, "", v528, __LINE__);
	return failed;
}

int variant386() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file180");
	FILE* v528 = fopen("file180", "wbx");
	failed = 0;
	fputs("[387/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1815 = fgets(v432, 64, v528);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb2c952b0, 0x00000000, 0, 1, 0x00000000, "", v528, __LINE__);
	return failed;
}

int variant387() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file180");
	FILE* v528 = fopen("file180", "wbx");
	failed = 0;
	fputs("[388/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1816 = fgets(v433, 64, v528);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xbb1b52b0, 0x00000000, 0, 1, 0x00000000, "", v528, __LINE__);
	return failed;
}

int variant388() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file180");
	FILE* v528 = fopen("file180", "wbx");
	failed = 0;
	fputs("[389/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1817 = fgets(v434, 64, v528);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x5cc8b2b0, 0x00000000, 0, 1, 0x00000000, "", v528, __LINE__);
	return failed;
}

int variant389() {
	int failed = 0;
	create_empty_file("empty180");
	FILE* v529 = fopen("empty180", "wbx");
	failed = 0;
	fputs("[390/432]fgets(NULL, Int(0), FILE* (Empty file, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1818 = fgets(NULL, 0, v529);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v529, __LINE__);
	return failed;
}

int variant390() {
	int failed = 0;
	create_empty_file("empty180");
	FILE* v529 = fopen("empty180", "wbx");
	failed = 0;
	fputs("[391/432]fgets(Random text, Int(0), FILE* (Empty file, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1819 = fgets("GwMvlkT_GhjaeqzUbjITupz3jQNUAuz0Qpi0z96rNYdZiPgavfwkn2xLDrPibC1o0_jApbPf1mEeInCL_8WzN52nbwmX2_t5OP8j", 0, v529);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v529, __LINE__);
	return failed;
}

int variant391() {
	int failed = 0;
	create_random_file("random180");
	FILE* v530 = fopen("random180", "wbx");
	failed = 0;
	fputs("[392/432]fgets(NULL, Int(0), FILE* (File with random text, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1823 = fgets(NULL, 0, v530);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v530, __LINE__);
	return failed;
}

int variant392() {
	int failed = 0;
	create_random_file("random180");
	FILE* v530 = fopen("random180", "wbx");
	failed = 0;
	fputs("[393/432]fgets(Random text, Int(0), FILE* (File with random text, \"wbx\"))\n", stdout);
	errno = 0;
	char* v1824 = fgets("NiR6C_ISzKoySs12KgP2RVy89ExWQjzx5JfI0n9Wbw5rWk82FxoJtpzTqmW_ApbLeOd3yrq24CqHAUBY7N5kBZtsmrksuGS270EU", 0, v530);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v530, __LINE__);
	return failed;
}

int variant393() {
	int failed = 0;
	remove_file("file181");
	FILE* v531 = fopen("file181", "abx");
	failed = 0;
	fputs("[394/432]fgets(NULL, Int(0), FILE* (File name, \"abx\"))\n", stdout);
	errno = 0;
	char* v1828 = fgets(NULL, 0, v531);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x13121260, 0x00000000, 0, 0, 0x00000000, "", v531, __LINE__);
	return failed;
}

int variant394() {
	int failed = 0;
	remove_file("file181");
	FILE* v531 = fopen("file181", "abx");
	failed = 0;
	fputs("[395/432]fgets(Random text, Int(0), FILE* (File name, \"abx\"))\n", stdout);
	errno = 0;
	char* v1829 = fgets("41iBeL36qIOr_TH_EAYu3XCnfnOfTQE0N0WlcCKXNcEYwGqw4wGYBIOq3fQYTKBDHMXbKPjLLK3lEUPpfeqAm7fovv6TVBU1GzXm", 0, v531);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x6f26c260, 0x00000000, 0, 0, 0x00000000, "", v531, __LINE__);
	return failed;
}

int variant395() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file181");
	FILE* v531 = fopen("file181", "abx");
	failed = 0;
	fputs("[396/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"abx\"))\n", stdout);
	errno = 0;
	char* v1830 = fgets(v432, 64, v531);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0x811e92b0, 0x00000000, 0, 1, 0x00000000, "", v531, __LINE__);
	return failed;
}

int variant396() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file181");
	FILE* v531 = fopen("file181", "abx");
	failed = 0;
	fputs("[397/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"abx\"))\n", stdout);
	errno = 0;
	char* v1831 = fgets(v433, 64, v531);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xe21032b0, 0x00000000, 0, 1, 0x00000000, "", v531, __LINE__);
	return failed;
}

int variant397() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file181");
	FILE* v531 = fopen("file181", "abx");
	failed = 0;
	fputs("[398/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"abx\"))\n", stdout);
	errno = 0;
	char* v1832 = fgets(v434, 64, v531);
	failed |= validate_errno(9, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb7f642b0, 0x00000000, 0, 1, 0x00000000, "", v531, __LINE__);
	return failed;
}

int variant398() {
	int failed = 0;
	create_empty_file("empty181");
	FILE* v532 = fopen("empty181", "abx");
	failed = 0;
	fputs("[399/432]fgets(NULL, Int(0), FILE* (Empty file, \"abx\"))\n", stdout);
	errno = 0;
	char* v1833 = fgets(NULL, 0, v532);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v532, __LINE__);
	return failed;
}

int variant399() {
	int failed = 0;
	create_empty_file("empty181");
	FILE* v532 = fopen("empty181", "abx");
	failed = 0;
	fputs("[400/432]fgets(Random text, Int(0), FILE* (Empty file, \"abx\"))\n", stdout);
	errno = 0;
	char* v1834 = fgets("wk6rUn9kmqJT2d9txcIhAkjMUYfE9Up77Cqpv9eIGprYdXM8lK9tJcCobbp3VydiFdiovc2dcFmWsW_wUZ6dms8nU8uQh1XadN0P", 0, v532);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v532, __LINE__);
	return failed;
}

int variant400() {
	int failed = 0;
	create_random_file("random181");
	FILE* v533 = fopen("random181", "abx");
	failed = 0;
	fputs("[401/432]fgets(NULL, Int(0), FILE* (File with random text, \"abx\"))\n", stdout);
	errno = 0;
	char* v1838 = fgets(NULL, 0, v533);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v533, __LINE__);
	return failed;
}

int variant401() {
	int failed = 0;
	create_random_file("random181");
	FILE* v533 = fopen("random181", "abx");
	failed = 0;
	fputs("[402/432]fgets(Random text, Int(0), FILE* (File with random text, \"abx\"))\n", stdout);
	errno = 0;
	char* v1839 = fgets("4__T6hKKpuIsArJ5t4h24_Q_FqoGhEBAQB8KwCxnjAMOLKdOa9fM9h1W5IpKcIdHMD2U8SCNUvj_ZEp7uodZG1u8nNzkAePoCUEI", 0, v533);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v533, __LINE__);
	return failed;
}

int variant402() {
	int failed = 0;
	remove_file("file182");
	FILE* v534 = fopen("file182", "r+bx");
	failed = 0;
	fputs("[403/432]fgets(NULL, Int(0), FILE* (File name, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1843 = fgets(NULL, 0, v534);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v534, __LINE__);
	return failed;
}

int variant403() {
	int failed = 0;
	remove_file("file182");
	FILE* v534 = fopen("file182", "r+bx");
	failed = 0;
	fputs("[404/432]fgets(Random text, Int(0), FILE* (File name, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1844 = fgets("58KOGAJXjqZ7KcypYew_X8ZFU58QlZQEd2ibLERtGyaHNU_RXTbxuyaQT9EnC7KVHc60PIuqwzGY8COSLhXgozqernGmxUuC4WLo", 0, v534);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v534, __LINE__);
	return failed;
}

int variant404() {
	int failed = 0;
	create_empty_file("empty182");
	FILE* v535 = fopen("empty182", "r+bx");
	failed = 0;
	fputs("[405/432]fgets(NULL, Int(0), FILE* (Empty file, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1848 = fgets(NULL, 0, v535);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xaca3a260, 0x00000000, 0, 0, 0x00000000, "", v535, __LINE__);
	return failed;
}

int variant405() {
	int failed = 0;
	create_empty_file("empty182");
	FILE* v535 = fopen("empty182", "r+bx");
	failed = 0;
	fputs("[406/432]fgets(Random text, Int(0), FILE* (Empty file, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1849 = fgets("FO83iMeyPSyTGbvs6mg7IDWr7Unn51pmcOMQfUn0Nq7eV3Wtd2KK1D5bjavzpUJJN_LYhYH3rOA4GG3OWBw002KwhAanrKQbHAu8", 0, v535);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x9d3fc260, 0x00000000, 0, 0, 0x00000000, "", v535, __LINE__);
	return failed;
}

int variant406() {
	int failed = 0;
	char* v432 = malloc(64);
	create_empty_file("empty182");
	FILE* v535 = fopen("empty182", "r+bx");
	failed = 0;
	fputs("[407/432]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1850 = fgets(v432, 64, v535);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x98e3a2b0, 0x00000000, 1, 0, 0x00000000, "", v535, __LINE__);
	return failed;
}

int variant407() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_empty_file("empty182");
	FILE* v535 = fopen("empty182", "r+bx");
	failed = 0;
	fputs("[408/432]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1851 = fgets(v433, 64, v535);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x006a52b0, 0x00000000, 1, 0, 0x00000000, "", v535, __LINE__);
	return failed;
}

int variant408() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_empty_file("empty182");
	FILE* v535 = fopen("empty182", "r+bx");
	failed = 0;
	fputs("[409/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1852 = fgets(v434, 64, v535);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x2a3032b0, 0x00000000, 1, 0, 0x00000000, "", v535, __LINE__);
	return failed;
}

int variant409() {
	int failed = 0;
	create_random_file("random182");
	FILE* v536 = fopen("random182", "r+bx");
	failed = 0;
	fputs("[410/432]fgets(NULL, Int(0), FILE* (File with random text, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1853 = fgets(NULL, 0, v536);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x138b9260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v536, __LINE__);
	return failed;
}

int variant410() {
	int failed = 0;
	create_random_file("random182");
	FILE* v536 = fopen("random182", "r+bx");
	failed = 0;
	fputs("[411/432]fgets(Random text, Int(0), FILE* (File with random text, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1854 = fgets("HNzfBXoX7U3CPFNLBwwLT6fn5TT0LBSbsnr4CwViyzQBGvL6dvvJvHRHGXSZYVMhdGs1MpG03hYh2uFlc5JjwIxPIrokuQlFZcdM", 0, v536);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x547e1260, 0x00000000, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v536, __LINE__);
	return failed;
}

int variant411() {
	int failed = 0;
	char* v432 = malloc(64);
	create_random_file("random182");
	FILE* v536 = fopen("random182", "r+bx");
	failed = 0;
	fputs("[412/432]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1855 = fgets(v432, 64, v536);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xfb3092b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v536, __LINE__);
	return failed;
}

int variant412() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	create_random_file("random182");
	FILE* v536 = fopen("random182", "r+bx");
	failed = 0;
	fputs("[413/432]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1856 = fgets(v433, 64, v536);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x610682b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v536, __LINE__);
	return failed;
}

int variant413() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	create_random_file("random182");
	FILE* v536 = fopen("random182", "r+bx");
	failed = 0;
	fputs("[414/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"r+bx\"))\n", stdout);
	errno = 0;
	char* v1857 = fgets(v434, 64, v536);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xdc0a32b0, 0x0000003f, 0, 0, 0x00000400, "30683871327377487972686e636145436258326879516a4e79634936664f4677424154797156635551346d786d436e45324f4a704843464d6153535067526451565975524b7a677070725a6b62335a5f5a433462564c696e376b595f4b6271673438316f5665794c4a504f4d7830666273645170577556785370534b3053677733573174574f517450745455456964586d6b44353865514d44687969436842747233485978364e7330757158345f665335544f4a574d467a4953627869575742755644727435317a39383050646f485159654851416969796673326a4a706c354a6b416a4143565257387a4c594e71317556514c7675445435707a525246484a67344f4e676e746d4f5558305655544776466d34657066575a364474465f59456732414e4e4d647544654d4851555471415352666b4b5a6b367941506e574757354d384938504d4c374d6237514939526d6a49596e754555434c45324f384d435f694c6d6c324a683048324a656d766145706876626279524d796270646b755f3036706d593338473677784e4c4a5173546e385361414e77585a63504a4372545654425971443971583351503734455374764437454d313168784e454d773336696d71664f6e785f6961436b546a664432555a39415473496c424342696c5134396e3955716c6152706e49503166594244687235613874645f574d593041744770465733595a6f66556b654e693044526e4754465071494c676e624c62546d5f484533306f417667444f32574c4b3231785750415a716a657975446d314554367449695631664f6661435441524c58693255744d6e36645a4870714e55444a4d4b564a6437656346346e726d4170356d4849756c5f465872725174527831695970594a6e3052326b4c566549795165334851454f6c68306a4654334d656872495175784c515279664165535a4c664a485756645a6b4b45663072347776546f796d484e4166624c54625a6a6e6f3239776d79675875355133486435556c7a4562527a4a76646f4332484a3063454e4e426f4b4e4530746370795f7735707473325a367a675368506730416e63577763456f78764f474550666b53543764526779613658695571564b71493661796a4c465768446c364a597a5f61644c35485f7548656a32737439576e5f52485633446c697a77674b43514444466b70505532767a4152714a54386d42747958466b506331734b766b48696879427436536b7068566b79376b5f496b6a5465485451744230634932376f69557a7644477273514541554a48396a49335a32464552416c776433636f494544507a71747a754a633642784951446b7137346b6147314e6f315f4378797a726b41464549544e587649333863396c77734c62346d7745424e4b5f743575596f3561673333776d587862", v536, __LINE__);
	return failed;
}

int variant414() {
	int failed = 0;
	remove_file("file183");
	FILE* v537 = fopen("file183", "w+bx");
	failed = 0;
	fputs("[415/432]fgets(NULL, Int(0), FILE* (File name, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1858 = fgets(NULL, 0, v537);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x853c1260, 0x00000000, 0, 0, 0x00000000, "", v537, __LINE__);
	return failed;
}

int variant415() {
	int failed = 0;
	remove_file("file183");
	FILE* v537 = fopen("file183", "w+bx");
	failed = 0;
	fputs("[416/432]fgets(Random text, Int(0), FILE* (File name, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1859 = fgets("0Hy6Ut7dWmN1LwF9vSZI7btqcagWLhxLEhNsxk6bg0KSPiblUAAr9mX1Zx6m7qXzTsvjCcznTmGDJbPKM_iXdZxetIE6ZC2XFvAd", 0, v537);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3296a260, 0x00000000, 0, 0, 0x00000000, "", v537, __LINE__);
	return failed;
}

int variant416() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file183");
	FILE* v537 = fopen("file183", "w+bx");
	failed = 0;
	fputs("[417/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1860 = fgets(v432, 64, v537);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x3f8f92b0, 0x00000000, 1, 0, 0x00000000, "", v537, __LINE__);
	return failed;
}

int variant417() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file183");
	FILE* v537 = fopen("file183", "w+bx");
	failed = 0;
	fputs("[418/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1861 = fgets(v433, 64, v537);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xeb7002b0, 0x00000000, 1, 0, 0x00000000, "", v537, __LINE__);
	return failed;
}

int variant418() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file183");
	FILE* v537 = fopen("file183", "w+bx");
	failed = 0;
	fputs("[419/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1862 = fgets(v434, 64, v537);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x334192b0, 0x00000000, 1, 0, 0x00000000, "", v537, __LINE__);
	return failed;
}

int variant419() {
	int failed = 0;
	create_empty_file("empty183");
	FILE* v538 = fopen("empty183", "w+bx");
	failed = 0;
	fputs("[420/432]fgets(NULL, Int(0), FILE* (Empty file, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1863 = fgets(NULL, 0, v538);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v538, __LINE__);
	return failed;
}

int variant420() {
	int failed = 0;
	create_empty_file("empty183");
	FILE* v538 = fopen("empty183", "w+bx");
	failed = 0;
	fputs("[421/432]fgets(Random text, Int(0), FILE* (Empty file, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1864 = fgets("R3PjinMYbhYp_TV8KVWu5HzJ7byd9X8vp4fHSmwYuJmUwhRD8ArHXgrB4E3_d1COz7C8XYPTag2J3DJ8yWYb9ogkVSusqW9khVrP", 0, v538);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v538, __LINE__);
	return failed;
}

int variant421() {
	int failed = 0;
	create_random_file("random183");
	FILE* v539 = fopen("random183", "w+bx");
	failed = 0;
	fputs("[422/432]fgets(NULL, Int(0), FILE* (File with random text, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1868 = fgets(NULL, 0, v539);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v539, __LINE__);
	return failed;
}

int variant422() {
	int failed = 0;
	create_random_file("random183");
	FILE* v539 = fopen("random183", "w+bx");
	failed = 0;
	fputs("[423/432]fgets(Random text, Int(0), FILE* (File with random text, \"w+bx\"))\n", stdout);
	errno = 0;
	char* v1869 = fgets("Zu7NdbJLRYxZdEvM01gpvfMti7FmnGI5bEODBelN2ocWwRyv9FZjJaiXVx7r1QFRV1X4nTV6bZQJ54KG2KTCgsH9WqbyD7iP_Cvr", 0, v539);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v539, __LINE__);
	return failed;
}

int variant423() {
	int failed = 0;
	remove_file("file184");
	FILE* v540 = fopen("file184", "a+bx");
	failed = 0;
	fputs("[424/432]fgets(NULL, Int(0), FILE* (File name, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1873 = fgets(NULL, 0, v540);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xd10e0260, 0x00000000, 0, 0, 0x00000000, "", v540, __LINE__);
	return failed;
}

int variant424() {
	int failed = 0;
	remove_file("file184");
	FILE* v540 = fopen("file184", "a+bx");
	failed = 0;
	fputs("[425/432]fgets(Random text, Int(0), FILE* (File name, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1874 = fgets("kEqBnyw5u8ALOihGhV7YTmWg_4HN_Qa9SZVxbs0hQP7qVwa7npRHteZn6rBwxSyEmJqy9SiOUXtRx0nSsR11EWrbxTR2gBxX2CTM", 0, v540);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x20af0260, 0x00000000, 0, 0, 0x00000000, "", v540, __LINE__);
	return failed;
}

int variant425() {
	int failed = 0;
	char* v432 = malloc(64);
	remove_file("file184");
	FILE* v540 = fopen("file184", "a+bx");
	failed = 0;
	fputs("[426/432]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1875 = fgets(v432, 64, v540);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x33f1b2b0, 0x00000000, 1, 0, 0x00000000, "", v540, __LINE__);
	return failed;
}

int variant426() {
	int failed = 0;
	char* v433 = malloc(64);
	memset(v433, 0, 64);
	remove_file("file184");
	FILE* v540 = fopen("file184", "a+bx");
	failed = 0;
	fputs("[427/432]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1876 = fgets(v433, 64, v540);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0xb42872b0, 0x00000000, 1, 0, 0x00000000, "", v540, __LINE__);
	return failed;
}

int variant427() {
	int failed = 0;
	char* v434 = malloc(64);
	memcpy(v434, rand_data, 64);
	remove_file("file184");
	FILE* v540 = fopen("file184", "a+bx");
	failed = 0;
	fputs("[428/432]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1877 = fgets(v434, 64, v540);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer((FILE*)0x56ca52b0, 0x00000000, 1, 0, 0x00000000, "", v540, __LINE__);
	return failed;
}

int variant428() {
	int failed = 0;
	create_empty_file("empty184");
	FILE* v541 = fopen("empty184", "a+bx");
	failed = 0;
	fputs("[429/432]fgets(NULL, Int(0), FILE* (Empty file, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1878 = fgets(NULL, 0, v541);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v541, __LINE__);
	return failed;
}

int variant429() {
	int failed = 0;
	create_empty_file("empty184");
	FILE* v541 = fopen("empty184", "a+bx");
	failed = 0;
	fputs("[430/432]fgets(Random text, Int(0), FILE* (Empty file, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1879 = fgets("DXw9sFKefRVIaEru6rkxKqsuOJe5TMf7WnukHjoqahiilFHkZQmsknvyRy4Bm8VI7aZeHRYjo0xym2tPJIqBZq62db6qg_ndyGwU", 0, v541);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v541, __LINE__);
	return failed;
}

int variant430() {
	int failed = 0;
	create_random_file("random184");
	FILE* v542 = fopen("random184", "a+bx");
	failed = 0;
	fputs("[431/432]fgets(NULL, Int(0), FILE* (File with random text, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1883 = fgets(NULL, 0, v542);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v542, __LINE__);
	return failed;
}

int variant431() {
	int failed = 0;
	create_random_file("random184");
	FILE* v542 = fopen("random184", "a+bx");
	failed = 0;
	fputs("[432/432]fgets(Random text, Int(0), FILE* (File with random text, \"a+bx\"))\n", stdout);
	errno = 0;
	char* v1884 = fgets("puSUXOglYjKnjhtOhXVuCsi6XaJ_rYaQl3hCA4DkTuBkMP7d9tGt6uEhYEGffoCOc1hOdoTp40QkYv81J6cQlSOTCcGVxO7NP6Vt", 0, v542);
	failed |= validate_errno(0, __LINE__);
	failed |= validate_file_pointer(NULL, 0, 0, 0, 0, "", v542, __LINE__);
	return failed;
}

int segfault0() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		remove_file("file149");
		FILE* v435 = fopen("file149", "r");
		char* v1350 = fgets(v432, 64, v435);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[1/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file149");
		FILE* v435 = fopen("file149", "r");
		char* v1351 = fgets(v433, 64, v435);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[2/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file149");
		FILE* v435 = fopen("file149", "r");
		char* v1352 = fgets(v434, 64, v435);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[3/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file152");
		FILE* v444 = fopen("file152", "r+");
		char* v1395 = fgets(v432, 64, v444);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[4/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r+\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file152");
		FILE* v444 = fopen("file152", "r+");
		char* v1396 = fgets(v433, 64, v444);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[5/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r+\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file152");
		FILE* v444 = fopen("file152", "r+");
		char* v1397 = fgets(v434, 64, v444);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[6/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r+\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file155");
		FILE* v453 = fopen("file155", "rt");
		char* v1440 = fgets(v432, 64, v453);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[7/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"rt\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file155");
		FILE* v453 = fopen("file155", "rt");
		char* v1441 = fgets(v433, 64, v453);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[8/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"rt\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file155");
		FILE* v453 = fopen("file155", "rt");
		char* v1442 = fgets(v434, 64, v453);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[9/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"rt\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file158");
		FILE* v462 = fopen("file158", "r+t");
		char* v1485 = fgets(v432, 64, v462);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[10/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r+t\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file158");
		FILE* v462 = fopen("file158", "r+t");
		char* v1486 = fgets(v433, 64, v462);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[11/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r+t\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file158");
		FILE* v462 = fopen("file158", "r+t");
		char* v1487 = fgets(v434, 64, v462);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[12/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r+t\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file161");
		FILE* v471 = fopen("file161", "rb");
		char* v1530 = fgets(v432, 64, v471);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[13/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"rb\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file161");
		FILE* v471 = fopen("file161", "rb");
		char* v1531 = fgets(v433, 64, v471);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[14/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"rb\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file161");
		FILE* v471 = fopen("file161", "rb");
		char* v1532 = fgets(v434, 64, v471);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[15/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"rb\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file164");
		FILE* v480 = fopen("file164", "r+b");
		char* v1575 = fgets(v432, 64, v480);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[16/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r+b\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file164");
		FILE* v480 = fopen("file164", "r+b");
		char* v1576 = fgets(v433, 64, v480);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[17/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r+b\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file164");
		FILE* v480 = fopen("file164", "r+b");
		char* v1577 = fgets(v434, 64, v480);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[18/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r+b\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file167");
		FILE* v489 = fopen("file167", "rx");
		char* v1620 = fgets(v432, 64, v489);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[19/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"rx\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file167");
		FILE* v489 = fopen("file167", "rx");
		char* v1621 = fgets(v433, 64, v489);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[20/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"rx\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file167");
		FILE* v489 = fopen("file167", "rx");
		char* v1622 = fgets(v434, 64, v489);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[21/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"rx\"))\n", stdout);
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
		char* v432 = malloc(64);
		create_empty_file("empty168");
		FILE* v493 = fopen("empty168", "wx");
		char* v1640 = fgets(v432, 64, v493);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[22/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"wx\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty168");
		FILE* v493 = fopen("empty168", "wx");
		char* v1641 = fgets(v433, 64, v493);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[23/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"wx\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty168");
		FILE* v493 = fopen("empty168", "wx");
		char* v1642 = fgets(v434, 64, v493);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[24/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"wx\"))\n", stdout);
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
		char* v432 = malloc(64);
		create_random_file("random168");
		FILE* v494 = fopen("random168", "wx");
		char* v1645 = fgets(v432, 64, v494);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[25/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"wx\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random168");
		FILE* v494 = fopen("random168", "wx");
		char* v1646 = fgets(v433, 64, v494);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[26/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"wx\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random168");
		FILE* v494 = fopen("random168", "wx");
		char* v1647 = fgets(v434, 64, v494);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[27/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"wx\"))\n", stdout);
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
		char* v432 = malloc(64);
		create_empty_file("empty169");
		FILE* v496 = fopen("empty169", "ax");
		char* v1655 = fgets(v432, 64, v496);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[28/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"ax\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty169");
		FILE* v496 = fopen("empty169", "ax");
		char* v1656 = fgets(v433, 64, v496);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[29/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"ax\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty169");
		FILE* v496 = fopen("empty169", "ax");
		char* v1657 = fgets(v434, 64, v496);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[30/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"ax\"))\n", stdout);
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
		char* v432 = malloc(64);
		create_random_file("random169");
		FILE* v497 = fopen("random169", "ax");
		char* v1660 = fgets(v432, 64, v497);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[31/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"ax\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random169");
		FILE* v497 = fopen("random169", "ax");
		char* v1661 = fgets(v433, 64, v497);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[32/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"ax\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random169");
		FILE* v497 = fopen("random169", "ax");
		char* v1662 = fgets(v434, 64, v497);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[33/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"ax\"))\n", stdout);
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
		char* v432 = malloc(64);
		remove_file("file170");
		FILE* v498 = fopen("file170", "r+x");
		char* v1665 = fgets(v432, 64, v498);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[34/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r+x\"))\n", stdout);
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
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file170");
		FILE* v498 = fopen("file170", "r+x");
		char* v1666 = fgets(v433, 64, v498);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[35/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r+x\"))\n", stdout);
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
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file170");
		FILE* v498 = fopen("file170", "r+x");
		char* v1667 = fgets(v434, 64, v498);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[36/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r+x\"))\n", stdout);
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
		char* v432 = malloc(64);
		create_empty_file("empty171");
		FILE* v502 = fopen("empty171", "w+x");
		char* v1685 = fgets(v432, 64, v502);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[37/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault37() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty171");
		FILE* v502 = fopen("empty171", "w+x");
		char* v1686 = fgets(v433, 64, v502);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[38/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault38() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty171");
		FILE* v502 = fopen("empty171", "w+x");
		char* v1687 = fgets(v434, 64, v502);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[39/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault39() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random171");
		FILE* v503 = fopen("random171", "w+x");
		char* v1690 = fgets(v432, 64, v503);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[40/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault40() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random171");
		FILE* v503 = fopen("random171", "w+x");
		char* v1691 = fgets(v433, 64, v503);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[41/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault41() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random171");
		FILE* v503 = fopen("random171", "w+x");
		char* v1692 = fgets(v434, 64, v503);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[42/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault42() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty172");
		FILE* v505 = fopen("empty172", "a+x");
		char* v1700 = fgets(v432, 64, v505);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[43/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault43() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty172");
		FILE* v505 = fopen("empty172", "a+x");
		char* v1701 = fgets(v433, 64, v505);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[44/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault44() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty172");
		FILE* v505 = fopen("empty172", "a+x");
		char* v1702 = fgets(v434, 64, v505);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[45/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault45() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random172");
		FILE* v506 = fopen("random172", "a+x");
		char* v1705 = fgets(v432, 64, v506);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[46/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault46() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random172");
		FILE* v506 = fopen("random172", "a+x");
		char* v1706 = fgets(v433, 64, v506);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[47/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault47() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random172");
		FILE* v506 = fopen("random172", "a+x");
		char* v1707 = fgets(v434, 64, v506);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[48/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a+x\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault48() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		remove_file("file173");
		FILE* v507 = fopen("file173", "rtx");
		char* v1710 = fgets(v432, 64, v507);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[49/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"rtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault49() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file173");
		FILE* v507 = fopen("file173", "rtx");
		char* v1711 = fgets(v433, 64, v507);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[50/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"rtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault50() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file173");
		FILE* v507 = fopen("file173", "rtx");
		char* v1712 = fgets(v434, 64, v507);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[51/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"rtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault51() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty174");
		FILE* v511 = fopen("empty174", "wtx");
		char* v1730 = fgets(v432, 64, v511);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[52/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault52() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty174");
		FILE* v511 = fopen("empty174", "wtx");
		char* v1731 = fgets(v433, 64, v511);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[53/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault53() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty174");
		FILE* v511 = fopen("empty174", "wtx");
		char* v1732 = fgets(v434, 64, v511);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[54/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault54() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random174");
		FILE* v512 = fopen("random174", "wtx");
		char* v1735 = fgets(v432, 64, v512);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[55/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault55() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random174");
		FILE* v512 = fopen("random174", "wtx");
		char* v1736 = fgets(v433, 64, v512);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[56/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault56() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random174");
		FILE* v512 = fopen("random174", "wtx");
		char* v1737 = fgets(v434, 64, v512);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[57/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"wtx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault57() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty175");
		FILE* v514 = fopen("empty175", "atx");
		char* v1745 = fgets(v432, 64, v514);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[58/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault58() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty175");
		FILE* v514 = fopen("empty175", "atx");
		char* v1746 = fgets(v433, 64, v514);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[59/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault59() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty175");
		FILE* v514 = fopen("empty175", "atx");
		char* v1747 = fgets(v434, 64, v514);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[60/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault60() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random175");
		FILE* v515 = fopen("random175", "atx");
		char* v1750 = fgets(v432, 64, v515);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[61/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault61() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random175");
		FILE* v515 = fopen("random175", "atx");
		char* v1751 = fgets(v433, 64, v515);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[62/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault62() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random175");
		FILE* v515 = fopen("random175", "atx");
		char* v1752 = fgets(v434, 64, v515);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[63/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"atx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault63() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		remove_file("file176");
		FILE* v516 = fopen("file176", "r+tx");
		char* v1755 = fgets(v432, 64, v516);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[64/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault64() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file176");
		FILE* v516 = fopen("file176", "r+tx");
		char* v1756 = fgets(v433, 64, v516);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[65/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault65() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file176");
		FILE* v516 = fopen("file176", "r+tx");
		char* v1757 = fgets(v434, 64, v516);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[66/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault66() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty177");
		FILE* v520 = fopen("empty177", "w+tx");
		char* v1775 = fgets(v432, 64, v520);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[67/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault67() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty177");
		FILE* v520 = fopen("empty177", "w+tx");
		char* v1776 = fgets(v433, 64, v520);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[68/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault68() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty177");
		FILE* v520 = fopen("empty177", "w+tx");
		char* v1777 = fgets(v434, 64, v520);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[69/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault69() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random177");
		FILE* v521 = fopen("random177", "w+tx");
		char* v1780 = fgets(v432, 64, v521);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[70/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault70() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random177");
		FILE* v521 = fopen("random177", "w+tx");
		char* v1781 = fgets(v433, 64, v521);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[71/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault71() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random177");
		FILE* v521 = fopen("random177", "w+tx");
		char* v1782 = fgets(v434, 64, v521);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[72/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault72() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty178");
		FILE* v523 = fopen("empty178", "a+tx");
		char* v1790 = fgets(v432, 64, v523);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[73/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault73() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty178");
		FILE* v523 = fopen("empty178", "a+tx");
		char* v1791 = fgets(v433, 64, v523);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[74/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault74() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty178");
		FILE* v523 = fopen("empty178", "a+tx");
		char* v1792 = fgets(v434, 64, v523);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[75/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault75() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random178");
		FILE* v524 = fopen("random178", "a+tx");
		char* v1795 = fgets(v432, 64, v524);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[76/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault76() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random178");
		FILE* v524 = fopen("random178", "a+tx");
		char* v1796 = fgets(v433, 64, v524);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[77/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault77() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random178");
		FILE* v524 = fopen("random178", "a+tx");
		char* v1797 = fgets(v434, 64, v524);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[78/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a+tx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault78() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		remove_file("file179");
		FILE* v525 = fopen("file179", "rbx");
		char* v1800 = fgets(v432, 64, v525);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[79/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"rbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault79() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file179");
		FILE* v525 = fopen("file179", "rbx");
		char* v1801 = fgets(v433, 64, v525);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[80/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"rbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault80() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file179");
		FILE* v525 = fopen("file179", "rbx");
		char* v1802 = fgets(v434, 64, v525);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[81/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"rbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault81() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty180");
		FILE* v529 = fopen("empty180", "wbx");
		char* v1820 = fgets(v432, 64, v529);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[82/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault82() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty180");
		FILE* v529 = fopen("empty180", "wbx");
		char* v1821 = fgets(v433, 64, v529);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[83/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault83() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty180");
		FILE* v529 = fopen("empty180", "wbx");
		char* v1822 = fgets(v434, 64, v529);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[84/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault84() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random180");
		FILE* v530 = fopen("random180", "wbx");
		char* v1825 = fgets(v432, 64, v530);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[85/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault85() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random180");
		FILE* v530 = fopen("random180", "wbx");
		char* v1826 = fgets(v433, 64, v530);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[86/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault86() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random180");
		FILE* v530 = fopen("random180", "wbx");
		char* v1827 = fgets(v434, 64, v530);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[87/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"wbx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault87() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty181");
		FILE* v532 = fopen("empty181", "abx");
		char* v1835 = fgets(v432, 64, v532);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[88/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault88() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty181");
		FILE* v532 = fopen("empty181", "abx");
		char* v1836 = fgets(v433, 64, v532);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[89/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault89() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty181");
		FILE* v532 = fopen("empty181", "abx");
		char* v1837 = fgets(v434, 64, v532);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[90/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault90() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random181");
		FILE* v533 = fopen("random181", "abx");
		char* v1840 = fgets(v432, 64, v533);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[91/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault91() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random181");
		FILE* v533 = fopen("random181", "abx");
		char* v1841 = fgets(v433, 64, v533);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[92/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault92() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random181");
		FILE* v533 = fopen("random181", "abx");
		char* v1842 = fgets(v434, 64, v533);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[93/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"abx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault93() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		remove_file("file182");
		FILE* v534 = fopen("file182", "r+bx");
		char* v1845 = fgets(v432, 64, v534);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[94/108]fgets(EmptyBuffer(64), Int(64), FILE* (File name, \"r+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault94() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		remove_file("file182");
		FILE* v534 = fopen("file182", "r+bx");
		char* v1846 = fgets(v433, 64, v534);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[95/108]fgets(ClearBuffer(64), Int(64), FILE* (File name, \"r+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault95() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		remove_file("file182");
		FILE* v534 = fopen("file182", "r+bx");
		char* v1847 = fgets(v434, 64, v534);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[96/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File name, \"r+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault96() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty183");
		FILE* v538 = fopen("empty183", "w+bx");
		char* v1865 = fgets(v432, 64, v538);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[97/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault97() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty183");
		FILE* v538 = fopen("empty183", "w+bx");
		char* v1866 = fgets(v433, 64, v538);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[98/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault98() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty183");
		FILE* v538 = fopen("empty183", "w+bx");
		char* v1867 = fgets(v434, 64, v538);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[99/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault99() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random183");
		FILE* v539 = fopen("random183", "w+bx");
		char* v1870 = fgets(v432, 64, v539);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[100/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault100() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random183");
		FILE* v539 = fopen("random183", "w+bx");
		char* v1871 = fgets(v433, 64, v539);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[101/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault101() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random183");
		FILE* v539 = fopen("random183", "w+bx");
		char* v1872 = fgets(v434, 64, v539);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[102/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"w+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault102() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_empty_file("empty184");
		FILE* v541 = fopen("empty184", "a+bx");
		char* v1880 = fgets(v432, 64, v541);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[103/108]fgets(EmptyBuffer(64), Int(64), FILE* (Empty file, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault103() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_empty_file("empty184");
		FILE* v541 = fopen("empty184", "a+bx");
		char* v1881 = fgets(v433, 64, v541);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[104/108]fgets(ClearBuffer(64), Int(64), FILE* (Empty file, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault104() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_empty_file("empty184");
		FILE* v541 = fopen("empty184", "a+bx");
		char* v1882 = fgets(v434, 64, v541);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[105/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (Empty file, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault105() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v432 = malloc(64);
		create_random_file("random184");
		FILE* v542 = fopen("random184", "a+bx");
		char* v1885 = fgets(v432, 64, v542);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[106/108]fgets(EmptyBuffer(64), Int(64), FILE* (File with random text, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault106() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v433 = malloc(64);
		memset(v433, 0, 64);
		create_random_file("random184");
		FILE* v542 = fopen("random184", "a+bx");
		char* v1886 = fgets(v433, 64, v542);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[107/108]fgets(ClearBuffer(64), Int(64), FILE* (File with random text, \"a+bx\"))\n", stdout);
		int failed = !WIFSIGNALED(exitcode) || WTERMSIG(exitcode) != SIGSEGV;
		if (failed && WIFSIGNALED(exitcode))
			printf("%08x", WTERMSIG(exitcode));
		if (failed)
			fputs("Segfault failed\n", stdout);
		return failed;
	}
}

int segfault107() {
	int exitcode = 0;
	if (fork() == 0) {
		char* v434 = malloc(64);
		memcpy(v434, rand_data, 64);
		create_random_file("random184");
		FILE* v542 = fopen("random184", "a+bx");
		char* v1887 = fgets(v434, 64, v542);
		exit(0);
	} else {
		wait(&exitcode);
		fputs("[108/108]fgets(RandomBufferGenerator(64), Int(64), FILE* (File with random text, \"a+bx\"))\n", stdout);
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
	error |= variant148();
	error |= variant149();
	error |= variant150();
	error |= variant151();
	error |= variant152();
	error |= variant153();
	error |= variant154();
	error |= variant155();
	error |= variant156();
	error |= variant157();
	error |= variant158();
	error |= variant159();
	error |= variant160();
	error |= variant161();
	error |= variant162();
	error |= variant163();
	error |= variant164();
	error |= variant165();
	error |= variant166();
	error |= variant167();
	error |= variant168();
	error |= variant169();
	error |= variant170();
	error |= variant171();
	error |= variant172();
	error |= variant173();
	error |= variant174();
	error |= variant175();
	error |= variant176();
	error |= variant177();
	error |= variant178();
	error |= variant179();
	error |= variant180();
	error |= variant181();
	error |= variant182();
	error |= variant183();
	error |= variant184();
	error |= variant185();
	error |= variant186();
	error |= variant187();
	error |= variant188();
	error |= variant189();
	error |= variant190();
	error |= variant191();
	error |= variant192();
	error |= variant193();
	error |= variant194();
	error |= variant195();
	error |= variant196();
	error |= variant197();
	error |= variant198();
	error |= variant199();
	error |= variant200();
	error |= variant201();
	error |= variant202();
	error |= variant203();
	error |= variant204();
	error |= variant205();
	error |= variant206();
	error |= variant207();
	error |= variant208();
	error |= variant209();
	error |= variant210();
	error |= variant211();
	error |= variant212();
	error |= variant213();
	error |= variant214();
	error |= variant215();
	error |= variant216();
	error |= variant217();
	error |= variant218();
	error |= variant219();
	error |= variant220();
	error |= variant221();
	error |= variant222();
	error |= variant223();
	error |= variant224();
	error |= variant225();
	error |= variant226();
	error |= variant227();
	error |= variant228();
	error |= variant229();
	error |= variant230();
	error |= variant231();
	error |= variant232();
	error |= variant233();
	error |= variant234();
	error |= variant235();
	error |= variant236();
	error |= variant237();
	error |= variant238();
	error |= variant239();
	error |= variant240();
	error |= variant241();
	error |= variant242();
	error |= variant243();
	error |= variant244();
	error |= variant245();
	error |= variant246();
	error |= variant247();
	error |= variant248();
	error |= variant249();
	error |= variant250();
	error |= variant251();
	error |= variant252();
	error |= variant253();
	error |= variant254();
	error |= variant255();
	error |= variant256();
	error |= variant257();
	error |= variant258();
	error |= variant259();
	error |= variant260();
	error |= variant261();
	error |= variant262();
	error |= variant263();
	error |= variant264();
	error |= variant265();
	error |= variant266();
	error |= variant267();
	error |= variant268();
	error |= variant269();
	error |= variant270();
	error |= variant271();
	error |= variant272();
	error |= variant273();
	error |= variant274();
	error |= variant275();
	error |= variant276();
	error |= variant277();
	error |= variant278();
	error |= variant279();
	error |= variant280();
	error |= variant281();
	error |= variant282();
	error |= variant283();
	error |= variant284();
	error |= variant285();
	error |= variant286();
	error |= variant287();
	error |= variant288();
	error |= variant289();
	error |= variant290();
	error |= variant291();
	error |= variant292();
	error |= variant293();
	error |= variant294();
	error |= variant295();
	error |= variant296();
	error |= variant297();
	error |= variant298();
	error |= variant299();
	error |= variant300();
	error |= variant301();
	error |= variant302();
	error |= variant303();
	error |= variant304();
	error |= variant305();
	error |= variant306();
	error |= variant307();
	error |= variant308();
	error |= variant309();
	error |= variant310();
	error |= variant311();
	error |= variant312();
	error |= variant313();
	error |= variant314();
	error |= variant315();
	error |= variant316();
	error |= variant317();
	error |= variant318();
	error |= variant319();
	error |= variant320();
	error |= variant321();
	error |= variant322();
	error |= variant323();
	error |= variant324();
	error |= variant325();
	error |= variant326();
	error |= variant327();
	error |= variant328();
	error |= variant329();
	error |= variant330();
	error |= variant331();
	error |= variant332();
	error |= variant333();
	error |= variant334();
	error |= variant335();
	error |= variant336();
	error |= variant337();
	error |= variant338();
	error |= variant339();
	error |= variant340();
	error |= variant341();
	error |= variant342();
	error |= variant343();
	error |= variant344();
	error |= variant345();
	error |= variant346();
	error |= variant347();
	error |= variant348();
	error |= variant349();
	error |= variant350();
	error |= variant351();
	error |= variant352();
	error |= variant353();
	error |= variant354();
	error |= variant355();
	error |= variant356();
	error |= variant357();
	error |= variant358();
	error |= variant359();
	error |= variant360();
	error |= variant361();
	error |= variant362();
	error |= variant363();
	error |= variant364();
	error |= variant365();
	error |= variant366();
	error |= variant367();
	error |= variant368();
	error |= variant369();
	error |= variant370();
	error |= variant371();
	error |= variant372();
	error |= variant373();
	error |= variant374();
	error |= variant375();
	error |= variant376();
	error |= variant377();
	error |= variant378();
	error |= variant379();
	error |= variant380();
	error |= variant381();
	error |= variant382();
	error |= variant383();
	error |= variant384();
	error |= variant385();
	error |= variant386();
	error |= variant387();
	error |= variant388();
	error |= variant389();
	error |= variant390();
	error |= variant391();
	error |= variant392();
	error |= variant393();
	error |= variant394();
	error |= variant395();
	error |= variant396();
	error |= variant397();
	error |= variant398();
	error |= variant399();
	error |= variant400();
	error |= variant401();
	error |= variant402();
	error |= variant403();
	error |= variant404();
	error |= variant405();
	error |= variant406();
	error |= variant407();
	error |= variant408();
	error |= variant409();
	error |= variant410();
	error |= variant411();
	error |= variant412();
	error |= variant413();
	error |= variant414();
	error |= variant415();
	error |= variant416();
	error |= variant417();
	error |= variant418();
	error |= variant419();
	error |= variant420();
	error |= variant421();
	error |= variant422();
	error |= variant423();
	error |= variant424();
	error |= variant425();
	error |= variant426();
	error |= variant427();
	error |= variant428();
	error |= variant429();
	error |= variant430();
	error |= variant431();
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
	error |= segfault37();
	error |= segfault38();
	error |= segfault39();
	error |= segfault40();
	error |= segfault41();
	error |= segfault42();
	error |= segfault43();
	error |= segfault44();
	error |= segfault45();
	error |= segfault46();
	error |= segfault47();
	error |= segfault48();
	error |= segfault49();
	error |= segfault50();
	error |= segfault51();
	error |= segfault52();
	error |= segfault53();
	error |= segfault54();
	error |= segfault55();
	error |= segfault56();
	error |= segfault57();
	error |= segfault58();
	error |= segfault59();
	error |= segfault60();
	error |= segfault61();
	error |= segfault62();
	error |= segfault63();
	error |= segfault64();
	error |= segfault65();
	error |= segfault66();
	error |= segfault67();
	error |= segfault68();
	error |= segfault69();
	error |= segfault70();
	error |= segfault71();
	error |= segfault72();
	error |= segfault73();
	error |= segfault74();
	error |= segfault75();
	error |= segfault76();
	error |= segfault77();
	error |= segfault78();
	error |= segfault79();
	error |= segfault80();
	error |= segfault81();
	error |= segfault82();
	error |= segfault83();
	error |= segfault84();
	error |= segfault85();
	error |= segfault86();
	error |= segfault87();
	error |= segfault88();
	error |= segfault89();
	error |= segfault90();
	error |= segfault91();
	error |= segfault92();
	error |= segfault93();
	error |= segfault94();
	error |= segfault95();
	error |= segfault96();
	error |= segfault97();
	error |= segfault98();
	error |= segfault99();
	error |= segfault100();
	error |= segfault101();
	error |= segfault102();
	error |= segfault103();
	error |= segfault104();
	error |= segfault105();
	error |= segfault106();
	error |= segfault107();
	return error;
}

