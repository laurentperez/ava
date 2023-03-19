# What is it ?

Ava is a general purpose bot/API interacting with NLP/LLM models.    
Ava is named after https://en.wikipedia.org/wiki/Ex_Machina_(film) and the _J_ ava Virtual Machine.

# What models or APIs does it support ?

Please note OpenAI's models are not the sole actors in LLMs ;)

Other models are : BLOOM, FB LLaMA, SF CodeGen, OPT-175B, BERT, PaLM, AlexaTM, Tabnine, Kite

However, OpenAI used human instruction training with InstructGPT (https://openai.com/research/instruction-following) so it's easier to prompt them than other models.  
In other words: *some public models are not instructed for straight question answering*: you need to prompt them using few-shot examples, user intentions, and think of the answer as a natural continuation of your prompt.      
See prompting tips at https://github.com/facebookresearch/llama/blob/main/FAQ.md#2-generations-are-bad

### Currently, supported models are :

- cloud/API based: ChatGPT-3+ (thru OpenAI APIs, an API key is required)
- local usage: BLOOM (thanks to Nouarame Tazi's work https://github.com/NouamaneTazi/bloomz.cpp)

*note: since FB LLaMA is a leaked model, the cat is out of the bag but no instructions will be provided on how to get the required files.*

# Overview

Ava has two components

- a bot: better suited for a chat flow, its default setup is to join your Mattermost server https://mattermost.com/ and wait for a user to chat with.
- a REST API: better suited for completions or general purpose interactions.

### Shall I use self-hosted models or cloud based models ?

Short answer : it depends.

*🕵️ Given the nature of NLP, the less a 3rd party vendor can inspect your usage the better*  
This has been recently covered by this UK GCHQ article : https://www.telegraph.co.uk/business/2023/03/14/gchq-warns-chatgpt-rival-chatbots-security-threat/

Also, *some* models are not *public or available locally* but models hosted on Huggingface models are, see : https://huggingface.co/bigscience

This is why Ava provides both cloud based usage and self-hosted local usage.

Why is it using Mattermost as a prefered chat relay ? because it is open source and self-hosted.  
Slack or Discord are not. For veterans, supporting [UnrealIRCd](https://www.unrealircd.org/) is of interest. For decentralized conversations, [Matrix](https://matrix.org/) is of iterest too.

# Considerations and responsible use of AI

Keep in mind *it should not be used for High-Stakes Decision-Making*, see below

- https://twitter.com/sama/status/1635136281952026625 (Sam Altman, "we definitely need more regulation on ai" )
- https://time.com/6247678/openai-chatgpt-kenya-workers/ (How did they make ChatGPT less toxic ?)
- https://oecd.ai/en/catalogue/tools/bigscience-bloom-responsible-ai-license-rail-1-0 (RAIL Responsible AI License)
- https://dl.acm.org/doi/10.1145/3442188.3445922 (On the Dangers of Stochastic Parrots: Can Language Models Be Too Big? 🦜)
- https://hal.archives-ouvertes.fr/hal-03368037/document (A systematic framework for describing ML’s effects on GHG emissions 🌱)
- https://learn.microsoft.com/en-us/legal/cognitive-services/openai/transparency-note#considerations-when-choosing-a-use-case (see Considerations)

# Quickstart

If you want to use local models downloaded from Huggingface : make sure you have enough RAM and disk space !  
For example the pytorch model of `bigscience/bloomz-7b1` weights at 14GB and ggml conversion requires around 10GB of RAM.

1. install a JDK17 from https://sdkman.io/
2. start the on-premise Mattermost chat server `docker compose up`
3. navigate to its http://localhost:8065, create yourself an account for demonstration purposes
4. create a team named `test` and an account `ava@ava.co` for the bot (hint: use "Invite people", copy the "Invite" invitation link in another browser window to create the bot account)  
5. hint: you can configure the bot team and credentials in `application.properties`
6. optional : `env export` a `CLIENT_OPENAI_SECRET_KEY` if you would like to use the OpenAI cloud models. Make sure you understand and cap your billing charges.
7. run Ava : `./gradlew quarkusDev`
8. the bot shall join the default chatroom "Town Square" and greet you: if it does not, check the server logs
9. open a Direct message with the bot, and chat with it.
10. if you want to use a Web UI for the REST API: it is located at http://localhost:8080/q/swagger-ui/


# Sample usage

- bot: TBD
- REST API:
```
curl -v -XPOST -H 'Content-Type: application/json' -d '{"msg":"translate \"Hi, how are you?\" in Spanish:"}' http://localhost:8080/hf/bloom
```
```
sampling parameters: temp = 0.800000, top_k = 40, top_p = 0.950000, repeat_last_n = 64, repeat_penalty = 1.300000  
translate "Hi, how are you?" in Spanish: Me encuentro muy bien. ¿Cómo estas tú? Yo estoy?: me alegro</s> [end of text]
```



# Frameworks & tools used

- https://quarkus.io/

If you are looking for other Java frameworks please consider Wenqi Glantz's work at https://github.com/wenqiglantz/chatgpt-whisper-spring-boot or Vayne's work at https://github.com/flashvayne/chatgpt-spring-boot-starter

- https://github.com/maruTA-bis5/mattermost4j
- Postgres for persistence

# License

MIT

