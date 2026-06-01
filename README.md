<div align="center">
  <img width="800" src="./src/main/resources/assets/minecraft/liquidbounce/logo_large.png" alt="GoldBounce">
  <br><br>
  <b>A high-performance, injection-based modification framework for Minecraft 1.8.9.</b>
  <br><br>
  <a href="https://discord.gg/RwmbBTtSPr">Discord</a> ｜ QQ: 445493864
  <br><br>
  <img src="https://img.shields.io/github/license/the-OmegaLabs/GoldBounce?style=flat-square&color=black" alt="License">
  <img src="https://img.shields.io/github/downloads/the-OmegaLabs/GoldBounce/total?style=flat-square&color=black" alt="Downloads">
</div>

<br>

> *本客户端的全部源代码可自由使用与修改。无需署名作者，惟请在文件头部添加项目链接以示出处。*
> *All source code can be freely used and modified. No attribution required; please include a repository link in file headers.*

## Architecture & Philosophy

GoldBounce is engineered as a robust, injection-based framework that redefines client-side architecture for Minecraft 1.8.9. Built with a strict emphasis on performance and packet-level security, the client intercepts and sanitizes inbound network traffic to mitigate risks associated with untrusted servers, including malicious payload injections and deserialization vulnerabilities.

Deviating from traditional, cluttered modification interfaces, the visual components—such as the TargetHUD and ESP modules—are heavily influenced by Modernist Swiss Design. We prioritize typographic hierarchy, precise OpenGL coordinate mapping, and minimal render overhead to achieve an industrial-grade, cinematic aesthetic.

Our engineering culture embraces AI-native workflows (Vibe Coding). We utilize large language models for complex algorithmic prototyping, automated static analysis of Java reflection calls, and maintaining a highly agile development cycle.

## Contributing

GoldBounce operates on an open-source model. We welcome pull requests that align with our architectural standards. Current technical priorities include:

- **JVM Bytecode Manipulation:** Optimizing event bus throughput and injection hooks.
- **Render Engine Overhaul:** Refactoring legacy GUI elements to modern, low-latency OpenGL implementations.
- **Protocol Security:** Implementing strict bounds-checking and network packet sanitization logic.

## Issue Tracker

For technical anomalies, crashes, or architectural proposals, please provide detailed stack traces and reproducible steps via the [Issue Tracker](https://github.com/the-OmegaLabs/GoldBounce/issues).

## License

This repository is distributed under the [GNU General Public License v3.0](LICENSE). 

**Key Directives:**
- You are permitted to use, modify, and distribute this software commercially or non-commercially.
- **Copyleft Obligation:** Modifications or derivative works must be strictly open-source. Using any code from this repository within closed-source or obfuscated environments is expressly prohibited.
- Derived works must adopt the identical GPLv3 license.
