#!/usr/bin/env bash
set -e

# Navigate to project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -d "$SCRIPT_DIR/allocationsystem" ]; then
    cd "$SCRIPT_DIR/allocationsystem"
else
    cd "$SCRIPT_DIR"
fi

# Load SDKMAN environment if present
if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk use java 17.0.12-tem 2>/dev/null || true
fi

# Force JAVA_HOME to Java 17 by searching candidate locations
for jdk in \
    "$HOME/.sdkman/candidates/java/17.0.12-tem" \
    "$HOME/.sdkman/candidates/java/current" \
    $HOME/.sdkman/candidates/java/17* \
    $HOME/.sdkman/candidates/java/*17* \
    /usr/lib/jvm/*17* \
    /usr/lib/jvm/*jdk-17*; do
    if [ -d "$jdk" ] && [ -x "$jdk/bin/javac" ]; then
        export JAVA_HOME="$jdk"
        export PATH="$JAVA_HOME/bin:$PATH"
        break
    fi
done

# If Java 17 is still not found, install it via sdkman
if [ -z "$JAVA_HOME" ] || ! "$JAVA_HOME/bin/javac" -version 2>&1 | grep -q " 17"; then
    if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        sdk install java 17.0.12-tem 2>/dev/null || true
        export JAVA_HOME="$HOME/.sdkman/candidates/java/17.0.12-tem"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

# Free up port 8080 if held by background process
fuser -k 8080/tcp 2>/dev/null || true

# Set Supabase database password
export DB_PASSWORD="${DB_PASSWORD:-collegeroomallocationsystem}"

echo "=================================================="
echo " Starting College Room Allocation System"
echo " JAVA_HOME    : $JAVA_HOME"
echo " Java Version : $(javac -version 2>&1)"
echo " Database     : Supabase PostgreSQL"
echo " Port         : 8080"
echo "=================================================="

# Run Spring Boot
./mvnw clean spring-boot:run
