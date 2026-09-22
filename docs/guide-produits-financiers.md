# Guide des produits financiers

*Document pédagogique — généré avec Claude, à jour au 2026-09-22.*

Un **produit financier** est simplement un contrat qui représente un droit sur de l'argent ou sur un actif : une part d'entreprise, une dette qu'on prête, une promesse d'échanger de l'argent à une date future. Chaque type de produit a ses propres règles du jeu — ce qu'on risque de perdre, ce qu'on peut gagner, et comment l'argent circule.

Ce guide part de zéro. Il explique, avec des exemples chiffrés simples, les produits financiers modélisés dans le projet **Finance_Analyzer** (un simulateur de portefeuille en Java) : les actions, ETF, obligations, cryptomonnaies, matières premières et devises (les produits « au comptant »), puis deux produits plus avancés, les **futures** et les **swaps de taux**.

L'idée n'est pas de devenir trader, mais de comprendre *pourquoi* chaque produit se comporte différemment quand son prix bouge — et de pouvoir le vérifier soi-même dans le simulateur.

## Le vocabulaire de base d'un portefeuille

Avant de parler des produits eux-mêmes, quelques mots reviennent tout le temps dans un portefeuille. Voici ce qu'ils veulent dire, avec un exemple qui reprend le scénario du simulateur (achat d'actions Apple).

| Terme | Ce que ça veut dire |
| --- | --- |
| **Cash** | L'argent disponible, non investi, dans le portefeuille |
| **Position** | Ce qu'on détient actuellement sur un produit donné (combien d'unités) |
| **Transaction** | Un achat ou une vente, à une date et un prix donnés |
| **Prix moyen d'achat** | Le prix moyen payé pour toutes les unités détenues, recalculé à chaque achat |
| **Valeur de marché** | Ce que vaudrait la position si on la revendait maintenant, au prix actuel |
| **P&L latent** (unrealized) | Le gain ou la perte *sur le papier*, tant qu'on n'a pas vendu |
| **P&L réalisé** | Le gain ou la perte *encaissé*, une fois la position vendue |

**Exemple chiffré.** On part avec 10 000 $ de cash. On achète 10 actions Apple à 320 $ : ça coûte 3 200 $, il reste donc 6 800 $ de cash, et la position est « 10 actions, prix moyen 320 $ ».

Si le prix d'Apple monte à 332,41 $, la valeur de marché de la position devient 10 × 332,41 = 3 324,10 $, et le P&L latent est de +124,10 $ (3 324,10 − 3 200). Rien n'est encore encaissé : si le prix redescend demain, ce gain peut disparaître. Il ne devient réel (réalisé) que le jour où on revend.

Si on achète encore 10 actions à 340 $, le prix moyen d'achat se recalcule : (10×320 + 10×340) / 20 = 330 $. C'est la moyenne pondérée par les quantités, pas une simple moyenne des deux prix.

## Les produits « au comptant »

Ces six produits ont un point commun essentiel : acheter coûte le plein montant (quantité × prix), et vendre rapporte le plein produit de la vente. Pas de surprise mécanique — ce qu'on voit est ce qu'on a.

| Produit | Classe Java | C'est quoi ? | Risque principal |
| --- | --- | --- | --- |
| **Action** (Stock) | `Stock` | Une part de propriété d'une entreprise cotée en bourse | Le cours peut baisser si l'entreprise va mal ou si le marché chute |
| **ETF** | `ETF` | Un fonds coté qui réplique un indice (ex. le S&P 500) en une seule ligne | Même risque que l'indice suivi, plus de petits frais de gestion annuels |
| **Obligation** (Bond) | `Bond` | Un prêt fait à une entreprise ou un État, remboursé à l'échéance avec des intérêts (le coupon) | Que l'emprunteur ne rembourse pas (risque de défaut), ou que les taux montent et fassent baisser sa valeur de revente |
| **Cryptomonnaie** (Crypto) | `Crypto` | Un actif numérique qui circule sur une blockchain (ex. Ethereum) | Très forte volatilité, marché peu régulé |
| **Matière première** (Commodity) | `Commodity` | Une ressource physique cotée (or, pétrole, blé…), par unité (once, baril, tonne) | Le prix dépend de l'offre/demande physique, météo, géopolitique |
| **Devise** (Forex) | `Forex` | Une paire de devises échangées l'une contre l'autre (ex. EUR/USD) | Les taux de change bougent avec les taux d'intérêt et l'économie de chaque pays |

Dans le code, tous ces produits héritent d'une classe commune `Asset`, et ne diffèrent que par les quelques informations propres à chacun (le secteur pour une action, le taux de coupon pour une obligation, etc.). C'est cette classe `Asset` qui définit aussi, par défaut, la règle « acheter coûte quantité × prix » — une règle que les deux produits suivants (Future et Swap) vont justement casser.

## Les produits dérivés à effet de levier : le Future

Un **future** (contrat à terme) est un engagement d'acheter ou de vendre un actif à une date future, à un prix fixé aujourd'hui. La grande différence avec une action : on ne paie pas le plein montant à l'achat, seulement une **marge initiale** — une fraction du montant total (souvent 10 à 20 %). C'est ce qui crée l'**effet de levier**.

**Autrement dit :** c'est comme réserver une voiture à 20 000 € en versant un acompte de 2 000 € (10 %) : le prix est bloqué aujourd'hui, mais tu ne paies que l'acompte tout de suite. Si la voiture vaut 22 000 € dans 3 mois, ton gain de 2 000 € représente 100 % de ton acompte — pas 10 % du prix. Si elle vaut 18 000 €, même chose en perte.

**Exemple chiffré.** Imaginons un future sur 10 actions Apple à 300 $, avec 20 % de marge initiale :

- Valeur notionnelle (l'exposition réelle) : 10 × 300 = 3 000 $
- Marge à payer pour ouvrir la position : 3 000 × 20 % = **600 $** seulement

Si le prix monte à 320 $, le gain se calcule sur la valeur notionnelle complète : (320 − 300) × 10 = **+200 $**. Rapporté aux 600 $ réellement engagés, c'est un gain de +33 % — alors que l'action elle-même n'a monté que de 6,7 %. C'est l'effet de levier : les gains (et les pertes) sont amplifiés par rapport au cash engagé.

C'est aussi ce qui rend les futures dangereux : si le prix baisse au lieu de monter, la perte est amplifiée de la même façon. Une **marge de maintenance** (plus basse que la marge initiale) fixe un seuil minimum à garder ; si les pertes font tomber la marge disponible en dessous de ce seuil, on reçoit un **appel de marge** (margin call) : il faut ajouter du cash, ou la position est fermée de force.

Dans le code, la classe `Future` redéfinit deux comportements hérités de `Asset` : `getCashRequirement` (n'immobilise que la marge, pas le plein montant) et `getCashReturned` (rend la marge plus le gain ou la perte réalisée à la clôture). Les méthodes `markToMarketPnL` et `isMarginCall` calculent respectivement le gain/perte latent et si un appel de marge doit se déclencher.

## Les produits dérivés de taux : le Swap

Un **swap de taux d'intérêt** est un accord entre deux parties qui échangent périodiquement des intérêts calculés sur un même montant de référence (le **notionnel**) — sans jamais s'échanger ce montant lui-même. L'une paie un **taux fixe**, l'autre un **taux variable** (indexé sur un taux de référence du marché, comme l'EURIBOR).

**À quoi ça sert dans la vraie vie ?** Une entreprise qui a emprunté à taux variable craint que les taux montent et fasse grimper ses mensualités. Elle peut « échanger » ce risque via un swap : elle paie désormais un taux fixe (prévisible) et reçoit le taux variable, qui vient compenser ce qu'elle doit sur son emprunt. C'est un outil de couverture (hedging), pas seulement de spéculation.

**Autrement dit :** toi et un·e ami·e avez chacun un abonnement calculé sur le même montant de référence — toi en forfait fixe, ton ami en forfait variable. Chaque mois, vous comparez ce que le forfait variable aurait coûté par rapport à ton montant fixe, et celui des deux qui est « perdant » ce mois-là paie la différence à l'autre. Vous ne vous transférez jamais le montant total du forfait, seulement l'écart, périodiquement.

**Exemple chiffré.** Notionnel de 1 000 000 $, taux fixe 3 %, règlement trimestriel (4 fois par an) :

- Jambe fixe par trimestre : 1 000 000 × 3 % / 4 = **7 500 $**
- Si le taux variable du trimestre est 2,5 % : jambe variable = 1 000 000 × 2,5 % / 4 = **6 250 $**

Celui qui paie le fixe et reçoit le variable règle la différence : 6 250 − 7 500 = **−1 250 $** (il paie 1 250 $ ce trimestre, puisque le taux variable est tombé sous le taux fixe). Si le taux variable était monté à 4 %, il aurait au contraire *reçu* 2 500 $.

Autre différence fondamentale avec les produits vus jusqu'ici : entrer dans un swap **ne coûte rien** au départ (pas de notionnel échangé), et un swap ne s'achète ni ne se vend comme une action — seuls les règlements périodiques font bouger le cash. Dans le code, la classe `InterestRateSwap` redéfinit `getCashRequirement` et `getCashReturned` pour renvoyer 0, et c'est la méthode `netSettlement` (appliquée via `Portfolio.settleSwap`) qui calcule et applique ce règlement périodique.

## Retrouver ces produits dans le projet Finance_Analyzer

Le simulateur `PortfolioApp` (module `com.financeanalyzer.ui`) rejoue l'historique de prix d'AAPL jour par jour, et permet de manipuler concrètement ces produits :

- Le sélecteur **Instrument** en bas de l'écran propose l'action AAPL (comptant) et un future sur AAPL (à levier). Acheter/vendre la même quantité des deux, sur le même mouvement de prix, permet de comparer directement le cash mobilisé et le P&L obtenu — exactement l'exemple chiffré de la section Future, mais avec les vrais prix historiques d'Apple.
- Le tableau des positions affiche une colonne **Type** (`AssetType`) pour voir en un coup d'œil ce qu'on détient.
- Le bouton **Swap de taux…** ouvre une calculette : on saisit un notionnel, un taux fixe, un taux variable et une fréquence, elle affiche la jambe fixe, la jambe variable et le règlement net, et peut l'appliquer réellement au cash du portefeuille.

Côté code, chaque produit est une classe dans `src/main/java/com/financeanalyzer/portfolio/` (`Stock`, `ETF`, `Bond`, `Crypto`, `Commodity`, `Forex`, `Future`, `InterestRateSwap`), toutes filles de `Asset`. Le comportement financier de chacun est testé indépendamment dans `src/test/java/.../portfolio/` (`FutureTest`, `InterestRateSwapTest`…), et le `README.md` du dépôt reprend ce même tableau des produits, version plus technique.

## Lexique express

- **Actif (Asset)** — tout produit financier qu'on peut détenir dans un portefeuille.
- **Appel de marge (margin call)** — demande de reconstituer sa garantie quand les pertes latentes deviennent trop importantes.
- **Cash** — l'argent disponible, non investi.
- **Coupon** — l'intérêt versé périodiquement par une obligation.
- **Effet de levier** — le fait qu'un petit montant engagé (la marge) contrôle une exposition bien plus grande, amplifiant gains et pertes.
- **Jambe fixe / jambe variable (swap)** — les deux flux d'intérêts échangés dans un swap de taux, l'un à taux constant, l'autre indexé sur un taux de marché.
- **Marge initiale / de maintenance** — le cash exigé à l'ouverture d'un future, et le seuil minimum à garder ensuite.
- **Notionnel** — le montant de référence sur lequel se calculent les flux d'un produit dérivé, sans être échangé lui-même.
- **P&L (Profit & Loss)** — le gain ou la perte d'une position ; *latent* tant qu'on n'a pas vendu, *réalisé* une fois vendu.
- **Position** — ce qu'on détient actuellement sur un produit donné.
- **Prix moyen d'achat** — le coût moyen, pondéré par les quantités, de toutes les unités achetées.
- **Taux variable de référence** — un taux d'intérêt de marché (ex. EURIBOR) auquel un produit peut être indexé.
- **Valeur de marché** — ce que vaudrait une position si on la clôturait maintenant, au prix courant.
- **Valeur notionnelle** — l'exposition réelle d'un contrat à terme (quantité × taille du contrat × prix), par opposition à la marge, bien plus faible, réellement engagée.
