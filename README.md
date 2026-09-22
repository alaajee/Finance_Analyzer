# Finance Analyzer

Projet pédagogique en Java pour comprendre le fonctionnement des différents
produits financiers : leurs caractéristiques, leurs risques, et comment ils se
comportent dans un portefeuille (mouvements de cash, valorisation, P&L).

Le simulateur (`com.financeanalyzer.ui.PortfolioApp`) rejoue l'historique de
prix d'AAPL jour par jour et permet d'acheter/vendre ces produits pour observer
concrètement leur mécanique.

> **Débutant·e en finance ?** Commence par [`docs/guide-produits-financiers.md`](docs/guide-produits-financiers.md) :
> un guide pédagogique qui explique chaque produit depuis zéro, avec des exemples chiffrés.

## Les produits financiers modélisés

Tous héritent de `Asset` (`src/main/java/com/financeanalyzer/portfolio/`), qui
définit deux points d'extension utilisés par `Portfolio` pour appliquer les
transactions :

- `getCashRequirement(quantity, price)` — le cash mobilisé à l'**ouverture** d'une position.
- `getCashReturned(quantity, entryPrice, exitPrice)` — le cash récupéré à la **clôture**,
  et aussi ce que vaut la position "si on la clôturait maintenant" (utilisé pour
  la valeur de marché et le P&L latent, voir `Position`).

Par défaut, les deux valent `quantité * prix` : c'est le comportement d'un achat
au comptant (cash equity). Chaque produit ci-dessous ne redéfinit ces méthodes
que lorsque sa mécanique réelle s'en écarte — c'est précisément ce qui les
distingue les uns des autres.

### Produits au comptant (cash)

| Classe | Ce qu'il représente | Particularité modélisée |
|---|---|---|
| `Stock` | Action d'une entreprise cotée | Secteur d'activité |
| `ETF` | Fonds indiciel coté qui réplique un indice | Indice sous-jacent, frais de gestion (`expenseRatio`) |
| `Bond` | Obligation : prêt à une entité, remboursé à échéance | Taux de coupon annuel, date d'échéance |
| `Crypto` | Cryptoactif échangé sur une blockchain | Réseau (Ethereum, Bitcoin, …) |
| `Commodity` | Matière première (or, pétrole, blé…) | Unité de cotation (once, baril, tonne…) |
| `Forex` | Paire de devises (ex. EUR/USD) | Devise de base / devise de cotation |

Pour tous ces produits, acheter immobilise le plein montant (`quantité * prix`)
et vendre restitue le plein produit de la vente : ce sont les valeurs par
défaut héritées de `Asset`, non redéfinies.

### Produits dérivés / à effet de levier

**`Future`** — un contrat à terme : l'engagement d'acheter/vendre un actif
sous-jacent à une échéance donnée, à un prix fixé aujourd'hui.

- On ne mobilise à l'ouverture que la **marge initiale** (une fraction de la
  valeur notionnelle, ex. 10-20 %), pas la valeur pleine du contrat — d'où
  l'effet de levier : un même mouvement de prix produit un gain/perte calculé
  sur la valeur notionnelle complète, mais rapporté à un cash engagé bien plus
  faible.
- `markToMarketPnL` calcule ce gain/perte latent, amplifié par la taille du
  contrat (`contractSize`).
- `isMarginCall` détecte quand la marge disponible (marge initiale + P&L
  latent) tombe sous la **marge de maintenance**, ce qui déclenche un appel de
  marge dans la réalité.
- `getCashRequirement` ne renvoie que la marge initiale ; `getCashReturned`
  renvoie la marge rendue plus le P&L réalisé à la clôture.

**`InterestRateSwap`** — un swap de taux d'intérêt : deux contreparties
échangent périodiquement des flux d'intérêts calculés sur un même montant
notionnel, l'une à taux fixe, l'autre à taux variable (indexé sur un taux de
référence comme l'EURIBOR).

- **Aucun capital n'est échangé** à l'entrée : `getCashRequirement` et
  `getCashReturned` renvoient 0. Un swap ne s'achète ni ne se vend comme une
  action.
- Seuls les **règlements périodiques** (le différentiel entre jambe fixe et
  jambe variable, `netSettlement`) génèrent un mouvement de cash — appliqué au
  portefeuille via `Portfolio.settleSwap(...)`, une méthode séparée
  d'`applyTransaction` puisqu'un swap ne crée pas de position au sens classique.
- Selon que l'on paie le fixe (`payerFixed = true`, pari sur une hausse des
  taux) ou qu'on le reçoit (pari sur une baisse), le signe du règlement
  s'inverse.

## Explorer les produits dans le simulateur

Dans `PortfolioApp` :

- Le sélecteur **Instrument** permet de choisir entre l'action AAPL (comptant)
  et un future sur AAPL (levier), et de les acheter/vendre au même flux de
  prix pour comparer directement le cash mobilisé et le P&L obtenu pour une
  même variation de prix.
- Le bouton **Swap de taux…** ouvre une calculette qui affiche la jambe fixe,
  la jambe variable et le règlement net pour des paramètres saisis librement,
  et peut appliquer ce règlement au cash du portefeuille.

## Tests

Chaque produit a ses tests de modélisation (validation des paramètres, calculs
propres au produit) dans `src/test/java/com/financeanalyzer/portfolio/` :
`AssetSubclassesTest`, `FutureTest`, `InterestRateSwapTest`. L'intégration avec
`Portfolio` (mouvements de cash à l'achat/vente/règlement) est couverte par
`PortfolioTest` (produits au comptant) et `PortfolioLeveragedProductsTest`
(future à effet de levier, swap).

```
mvn test
```
