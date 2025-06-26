# Coding Standards

<div align = "center">
    <h1>DO NOT USE KOTLIN</h1>
</div>

# Project GoldBounce Coding Standards

Everyone is invited to participate in the development of GoldBounce with pull requests and to open issues on our [separate repository](about:blank). However, we have to enforce certain standards to keep our code readable, consistent and easier to maintain.

We kindly ask you to use Java instead of Kotlin for new code, if possible. In the long term, it is our goal to largely migrate GoldBounce to Java.

Contributors: https://github.com/bzym2/GoldBounce/graphs/contributors

## General
This section lists the official conventions of the languages Java and Kotlin. This project tries to follow them as closely as possible and we expect outside developers to do the same when working on the client.

**Additional, non-standard conventions are listed below. These must also be followed.**

### Java
* Have a look at Oracle's [Java Code PDF document](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf).
* Read the Wikipedia article on [Java's Syntax](https://en.wikipedia.org/wiki/Java_syntax).
* Look at Oracle's [Java Tutorial](https://docs.oracle.com/javase/tutorial/java/).

# Rewriting
If parts of the codebase that are currently still written in Kotlin can be ported to Java without changing its behaviour, you are welcome to do so. However, please do not simply rely on some java decompilers, optimize the decompiled code first.

# Files
### Generation

To document the ownership of a file, we include the following text in all code files *(.java and .kt)* at the beginning of the file:

```java
/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
```
 
### Tags
`@author <author-name>` tags are allowed, but minimize its usage as much as possible.

No other tags are allowed.

# Packages
### Naming
Our naming of packages follows the following format:
* `country.company-name.product-name`

*Example:* 
* `net.ccbluex.liquidbounce`

If your code is self-contained and not designed exclusively for GoldBounce, we may allow you to include it in a separate package outside `net.ccbluex.liquidbounce`. Please note that we have to decide on a case by case basis.
  
*Example:*
`net.vitox` instead of `net.ccbluex`

Links:

* [Java Package information at WikiPedia](https://en.wikipedia.org/wiki/Java_package ).
