#!/bin/sh

FAILED=0
STEP_MESSAGES=

fail() {
    FAILED=$((FAILED + 1))
    STEP_MESSAGES="${STEP_MESSAGES}  Step \"$1\" failed with code $2
"
}

run_codespell() {
    codespell app/src -S "app/src/main/res/values-*" || fail codespell $?
}

run_ktlint() {
    ktlint || fail ktlint $?
}

all() {
    run_codespell
    run_ktlint
}

main() {
    set -xeu

    if [ $# -ge 1 ]; then
        "run_$(printf '%s' "$1" | tr '-' '_')"
    else
        all
    fi

    if [ $FAILED -eq 0 ]; then
        printf "\n\e[92mSUMMARY: All jobs succesfull\e[0m\n"
    else
        printf "\n\e[91mSUMMARY (%d %s failed):\e[0m\n%s" $FAILED "job$([ $FAILED -ne 1 ] && printf s)" "$STEP_MESSAGES"
    fi

    return $FAILED
}

main "$@"
