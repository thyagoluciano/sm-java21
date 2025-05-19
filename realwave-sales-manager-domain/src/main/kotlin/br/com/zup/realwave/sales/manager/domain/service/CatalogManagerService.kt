package br.com.zup.realwave.sales.manager.domain.service

import br.com.zup.realwave.sales.manager.domain.Item

interface CatalogManagerService {

    fun validateOffers(item: MutableSet<Item>)

}
