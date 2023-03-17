# What is it ?

Ava is a general purpose bot/API interacting with NLP models using APIs.  
Ava is named after https://en.wikipedia.org/wiki/Ex_Machina_(film) and the _J_ ava Virtual Machine.

Target models are : BLOOM, GPT-J/Neo, GPT-3/Codex, CodeGen, OPT-175B, BERT, PaLM, AlexaTM, Tabnine, Kite & all

Currently, supported models are : GPT-3+ (thru OpenAI APIs)

# Overview

It has two components

- a bot : better suited for a chat flow, its default setup is to join your Mattermost server https://mattermost.com/ and wait for a user to chat with.

Q&A : Why Mattermost ? because it's open source and self-hosted. Slack or Discord are not.  
Given the nature of NLP, the less a 3rd party vendor can inspect your usage the better.  
Of course OpenAI models aren't public, but Huggingace models are : https://huggingface.co/docs/hub/models-downloading

- a REST API : better suited for completions or general purpose interactions. 

# Considerations and responsible use of AI

Keep in mind *it should not be used for High-Stakes Decision-Making*, see below

Please read : 

- https://twitter.com/sama/status/1635136281952026625 (Sam Altman, "we definitely need more regulation on ai" )
- https://time.com/6247678/openai-chatgpt-kenya-workers/ (How did they make ChatGPT less toxic)
- https://oecd.ai/en/catalogue/tools/bigscience-bloom-responsible-ai-license-rail-1-0 (RAIL Responsible AI License)
- https://dl.acm.org/doi/10.1145/3442188.3445922 (On the Dangers of Stochastic Parrots: Can Language Models Be Too Big? 🦜)
- https://hal.archives-ouvertes.fr/hal-03368037/document (A systematic framework for describing ML’s effects on GHG emissions 🌱)
- https://learn.microsoft.com/en-us/legal/cognitive-services/openai/transparency-note#considerations-when-choosing-a-use-case (see Considerations)

# Quickstart

1. install a JDK17 from https://sdkman.io/
2. start the on-premise Mattermost chat server `docker compose up`
3. navigate to its http://localhost:8065, create yourself an account
4. create a team named `test` and an account `ava@ava.co` for the bot (hint: use "Invite people", copy the "Invite" invitation link in another browser window to create the bot account)  
5. hint : you can reconfigure the bot team and credentials in `application.properties`
6. optional : `env export` a `CLIENT_OPENAI_SECRET_KEY` if you'd like to use the OpenAI cloud models. Make sure you understand and cap your billing charges.
7. run Ava : `./gradlew quarkusDev`
8. the bot shall join the default chatroom "Town Square" and greet you : if it does not, check the server logs
9. open a Direct message with the bot, and chat with it.

# Usage

TBD

# Frameworks used

- https://quarkus.io/

If you're looking for another framework please consider Wenqi Glantz's work at https://github.com/wenqiglantz/chatgpt-whisper-spring-boot or Vayne's work at https://github.com/flashvayne/chatgpt-spring-boot-starter

- https://github.com/maruTA-bis5/mattermost4j
- Postgres for persistence

# License

MIT

