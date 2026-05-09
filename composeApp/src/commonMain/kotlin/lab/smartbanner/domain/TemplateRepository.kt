package lab.smartbanner.domain

import lab.smartbanner.model.PosterTemplate

interface TemplateRepository {
    suspend fun getTemplates(): List<PosterTemplate>
    suspend fun getTemplateById(id: String): PosterTemplate?
}
