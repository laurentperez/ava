# What is it ?

Ava is a general purpose bot interacting with NLP models or APIs. 

Think BLOOM, GPT-J, GPT-2/3, Codex, CodeGen, OPT-175B, BERT, PaLM, AlexaTM, Tabnine, Kite & all

Ava is named after https://en.wikipedia.org/wiki/Ex_Machina_(film) and the _J_ ava Virtual Machine.

Keep in mind _it should not be used for High-Stakes Decision-Making_, see below

# Considerations and responsible use of AI

Please read : 

- https://oecd.ai/en/catalogue/tools/bigscience-bloom-responsible-ai-license-rail-1-0 (RAIL Responsible AI License)
- https://dl.acm.org/doi/10.1145/3442188.3445922 (On the Dangers of Stochastic Parrots: Can Language Models Be Too Big? 🦜)
- https://hal.archives-ouvertes.fr/hal-03368037/document (A systematic framework for describing ML’s effects on GHG emissions 🌱)
- https://learn.microsoft.com/en-us/legal/cognitive-services/openai/transparency-note#considerations-when-choosing-a-use-case (see Considerations)
- https://partnershiponai.org/paper/responsible-publication-recommendations/?fbclid=IwAR1pydsi5uj5H3efmJoyJ1LD8-_DZjtQPraO1xpkue74kGTbnUsJAeBGNts (Six Recommendations for Responsible Publication)

# Quickstart

1. install JDK17 and gradle from https://sdkman.io/
2. start the on-premise Mattermost chat server `docker compose up`
3. navigate to its http://localhost:8065, create yourself an account
4. create a team named `test` and an account `ava@ava.co` for the Ava bot (hint: use "Invite people", copy the "Invite" invitation link in another browser window to create the bot account)  
5. you can reconfigure the bot credentials in `application.properties` or `application.yml`
6. optional : env export your `CLIENT_OPENAI_SECRET_KEY` if you'd like to use the OpenAI/Codex cloud models. Make sure you understand and cap your billing charges.
7. run Ava : `gradle quarkusDev` or `gradle bootRun`
8. the bot shall join the default chatroom "Town Square" and greet you : if it does not, check the server logs
9. open a Direct message with the bot, and chat with it.

# Usage

TBD

# Java frameworks used

It treats both Quarkus and Spring as equal citizens

# License

MIT

