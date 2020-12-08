
char itoa_buffer[256];

const char* builtin_itoa(int x) {
	itoa_buffer[255] = '\0';
	char* ptr = itoa_buffer + 254;
	do {
		*ptr-- = (x % 10) + '0';
		x /= 10;
	} while (x);
	return ptr + 1;
}

int builtin_strcmp(const char* a, const char* b) {
	int diff;
	do {
		diff = *a++ - *b++;
		if (diff != 0)
			return diff;
	}
	while (*a != '\0');
	return *a - *b;
}

void remove_file(const char* filename) {
    remove(filename);
}

void create_empty_file(const char* filename) {
    remove(filename);
    fclose(fopen(filename, "w"));
}

const char* rand_data = "0h8q2swHyrhncaECbX2hyQjNycI6fOFwBATyqVcUQ4mxmCnE2OJpHCFMaSSPgRdQVYuRKzgpprZkb3Z_ZC4bVLin7kY_Kb"
"qg481oVeyLJPOMx0fbsdQpWuVxSpSK0Sgw3W1tWOQtPtTUEidXmkD58eQMDhyiChBtr3HYx6Ns0uqX4_fS5TOJWMFzISbxiWWBuVDrt51z980PdoHQYeHQ"
"Aiiyfs2jJpl5JkAjACVRW8zLYNq1uVQLvuDT5pzRRFHJg4ONgntmOUX0VUTGvFm4epfWZ6DtF_YEg2ANNMduDeMHQUTqASRfkKZk6yAPnWGW5M8I8PML7M"
"b7QI9RmjIYnuEUCLE2O8MC_iLml2Jh0H2JemvaEphvbbyRMybpdku_06pmY38G6wxNLJQsTn8SaANwXZcPJCrTVTBYqD9qX3QP74EStvD7EM11hxNEMw36"
"imqfOnx_iaCkTjfD2UZ9ATsIlBCBilQ49n9UqlaRpnIP1fYBDhr5a8td_WMY0AtGpFW3YZofUkeNi0DRnGTFPqILgnbLbTm_HE30oAvgDO2WLK21xWPAZq"
"jeyuDm1ET6tIiV1fOfaCTARLXi2UtMn6dZHpqNUDJMKVJd7ecF4nrmAp5mHIul_FXrrQtRx1iYpYJn0R2kLVeIyQe3HQEOlh0jFT3MehrIQuxLQRyfAeSZ"
"LfJHWVdZkKEf0r4wvToymHNAfbLTbZjno29wmygXu5Q3Hd5UlzEbRzJvdoC2HJ0cENNBoKNE0tcpy_w5pts2Z6zgShPg0AncWwcEoxvOGEPfkST7dRgya6"
"XiUqVKqI6ayjLFWhDl6JYz_adL5H_uHej2st9Wn_RHV3DlizwgKCQDDFkpPU2vzARqJT8mBtyXFkPc1sKvkHihyBt6SkphVky7k_IkjTeHTQtB0cI27oiU"
"zvDGrsQEAUJH9jI3Z2FERAlwd3coIEDPzqtzuJc6BxIQDkq74kaG1No1_CxyzrkAFEITNXvI38c9lwsLb4mwEBNK_t5uYo5ag33wmXxb";


void create_random_file(const char* filename) {
    FILE* file = fopen(filename, "w");
    fputs(rand_data, file);
    fclose(file);
}

#define ERR_MSG(MSG, LINE, EXP, GOT) \
        fputs(MSG " at line ", stdout);\
        fputs(builtin_itoa(line), stdout);\
        fputs(":\n", stdout);\
        fputs("Expected: ", stdout);\
        fputs(EXP, stdout);\
        fputs("\nGot:      ", stdout);\
        fputs(GOT, stdout);\
        fputs("\n", stdout)

int validate_errno(int expected, int line) {
    if (expected != errno)
        ERR_MSG("Errno verification failed", line, builtin_itoa(expected), builtin_itoa(errno));
        return 1;
    return 0;
}

int validate_int(int expected, int got, int line) {
    if (expected != got) {
        ERR_MSG("Int verification failed", line, builtin_itoa(expected), builtin_itoa(errno));
        return 1;
    }
    return 0;
}

void record_pointer(void* data) {
    printf("%08x\n", (unsigned long)data);
}


int validate_pointer(void* expected, void* got, int line) {
    int passed = (!expected && !got) || (expected && got);
    if (!passed) {
        fputs("Pointer verification failed at line ", stdout);
        fputs(builtin_itoa(line), stdout);
        fputs(":\n", stdout);
        fputs("Expected: ", stdout);
        if (expected)
            fputs("not ", stdout);
        fputs("NULL\nGot:      ", stdout);
        if (got)
            fputs("not ", stdout);
        fputs("NULL\n", stdout);
        return 1;
    }
    return 0;
}





void record_file_by_path(const char* path) {}

void record_file_by_pointer(FILE* file) {
    printf("%08x", (unsigned long)file);

    if (file != NULL) {
        printf(" %08x ", ftell(file)); // Position

        printf("%d ", feof(file)); // EOF

        printf("%d ", ferror(file)); // Error

        fseek(file, 0, SEEK_END);
        printf("%08x ", ftell(file)); // Size

        rewind(file);
        for (;;) {
            int c = fgetc(file);
            if (c == EOF)
                break;
            printf("%02x", c);
        }
    }
}

int unhexlify_char(char x) {
    if (x >= '0' && x <= '9')
        return x - '0';
    if (x >= 'a' && x <= 'f')
        return x - 'a' + 10;
    if (x >= 'A' && x <= 'F')
        return x - 'A' + 10;
    return -1;
}

char unhexlify_ptr(const char* x) {
    int h = unhexlify_char(x[0]);
    int l = unhexlify_char(x[1]);
    return (h << 4) | l;
}

int validate_file_pointer(FILE* expected, long int position, int eof, int error, long int size, const char* data, FILE* got, int line) {
    int result = validate_pointer(expected, got, line);
    if (expected && got) {
        long int got_position = ftell(got);
        int got_eof = feof(got);
        int got_error = ferror(got);
        fseek(got, 0, SEEK_END);
        long int got_size = ftell(got);

        if (got_position != position) {
            ERR_MSG("FILE* buffer position verification failed", line, builtin_itoa(position), builtin_itoa(got_position));
            result = 1;
        }

        if (got_eof != eof) {
            ERR_MSG("FILE* buffer EOF flag verification failed", line, builtin_itoa(eof), builtin_itoa(got_eof));
            result = 1;
        }

        if (got_error != error) {
            ERR_MSG("FILE* buffer error flag verification failed", line, builtin_itoa(error), builtin_itoa(got_error));
            result = 1;
        }

        if (got_size != size) {
            ERR_MSG("FILE* size verification failed", line, builtin_itoa(size), builtin_itoa(got_size));
            result = 1;
        }

        rewind(got);
        const char* ptr;
        for (ptr = data; *ptr; ptr+=2) {
            int c = fgetc(got);
            if (c == EOF)
                break;
            int x = unhexlify_ptr(ptr);
            if (c != x) {
                fputs("FILE* data verification failed at line ", stdout);
                fputs(builtin_itoa(line), stdout);
                fputs(":\nAt position ", stdout);
                fputs(builtin_itoa((ptr - data)/2), stdout);
                fputs("\nExpected: '", stdout);
                fputc(x, stdout);
                fputs("' (", stdout);
                printf("%08X", x);
                fputs(")\nGot:      '", stdout);
                fputc(c, stdout);
                fputs("' (", stdout);
                printf("%08X", c);
                fputs(")\n", stdout);
                return 1;
            }
        }
        if (*ptr) {
            fputs("FILE* data verification failed at line ", stdout);
            fputs(builtin_itoa(line), stdout);
            fputs(":\nAt position ", stdout);
            fputs(builtin_itoa((ptr - data)/2), stdout);
            fputs("\nExpected: '", stdout);
            printf("<%s>", ptr);
            fputc(unhexlify_ptr(ptr), stdout);
            fputs("' (", stdout);
            printf("%08X", unhexlify_ptr(ptr));
            fputs(")\nGot:      EOF (", stdout);
            printf("%08X", EOF);
            fputs(")\n", stdout);
            return 1;
        } else {
            int c = fgetc(got);
            if (c != EOF) {
                fputs("FILE* data verification failed at line ", stdout);
                fputs(builtin_itoa(line), stdout);
                fputs(":\nAt position ", stdout);
                fputs(builtin_itoa((ptr - data)/2), stdout);
                fputs("\nExpected: EOF (", stdout);
                printf("%08X", EOF);
                fputs(")\nGot:      '", stdout);
                fputc(c, stdout);
                fputs("' (", stdout);
                printf("%08X", c);
                fputs(")\n", stdout);
                return 1;
            }
        }
    }
    return result;
}