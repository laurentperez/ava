package fr.ava.ia.service.oai

class OpenAIAssistants {

    companion object {
        const val ASSISTANT_PYTHON_CONSISE = "you are a python code assistant." +
                "be concise. do not reply to me politely. do not explain the generated code." +
                "if you reply to me with python code, make sure the reply is formatted using ```python"

        const val ASSISTANT_PYTHON_USING = "using python, "

        // do not use in chat but codex code-davinci completion !
        const val ASSISTANT_PYTHON_COMPLETION = "\"\"\"python language, %PROMPT%\"\"\""
    }
}
