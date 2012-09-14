import taxotests.service.TagTranslationService

// Place your Spring DSL code here
beans = {
    taxonomyService(TagTranslationService) {bean->
        bean.autowire='byName'
    }
}
