/* Spike for #144. Built twice — once -arch arm64, once -arch x86_64 — and run.
 *
 * `sysctl.proc_translated` is 1 only for a process Rosetta 2 is translating.
 * `uname -m` from inside the process reports the arch the process believes it
 * is. Together they distinguish "an x86_64 binary really executed" from "the
 * shell quietly ran something arm64". */
#include <stdio.h>
#include <string.h>
#include <sys/sysctl.h>
#include <sys/utsname.h>

int main(void) {
    int translated = 0;
    size_t size = sizeof(translated);
    int rc = sysctlbyname("sysctl.proc_translated", &translated, &size, NULL, 0);
    struct utsname u;
    memset(&u, 0, sizeof(u));
    uname(&u);
    printf("PROBE machine=%s proc_translated=%d (sysctl rc=%d)\n",
           u.machine, translated, rc);
    return 0;
}
